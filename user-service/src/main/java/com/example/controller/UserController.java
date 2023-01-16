package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.R;
import com.example.entity.User;
import com.example.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RequestMapping("/user")
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/login")
    public R<User> login(HttpSession session, @RequestBody Map map){
        //这里用session实现登录功能
        //log.info(map.toString());//通过map也可以进行json数据接收
        //System.out.println("进来了");
        String account = map.get("account").toString();

        String password = map.get("password").toString();

        //Object attribute = session.getAttribute(phone);
        //这个phone就从redis中进行获取了
        User attribute = (User)redisTemplate.opsForValue().get(account);

        if(attribute!=null && attribute.getPassword().equals(password)){
            //说明redis里面存在账号密码，登录即可

            session.setAttribute("user",attribute.getId());

            return R.success(attribute);
        }
        //如果redis没有命中，就需要去数据库中查找
        //如果存在就登录，并存到redis中，如果不存在就返回登录失败
        LambdaQueryWrapper<User> wrap = new LambdaQueryWrapper<User>();
        wrap.eq(User::getAccount,account)
                .eq(User::getPassword,password);

        User uu=userService.getOne(wrap);
        if(uu != null){
            //说明用户存在,存入redis并返回即可
            //暂时不设置过期时间
            redisTemplate.opsForValue().set(uu.getAccount(),uu);
            session.setAttribute("user",uu.getId());
            return R.success(uu);
        }

        return R.error("账号或密码错误，请重试");
    }
    //注册功能
    @PostMapping("/register")
    public R<String> register(@RequestBody Map map){
        String account = map.get("account").toString();

        String password = map.get("password").toString();

        User user = new User();
        user.setAccount(account);
        user.setPassword(password);

        userService.save(user);//mp的根据自动雪花算法会生成用户id

        return R.success("注册成功，请返回登录");
    }
}
