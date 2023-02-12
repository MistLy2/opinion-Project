package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.example.Dto.UpdateDto;
import com.example.Dto.UserDto;
import com.example.config.R;
import com.example.entity.User;
import com.example.service.UserService;
import com.example.config.BaseContext;

import com.example.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RequestMapping("/user")
@RestController
@Configuration
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/login")
    public R<UserDto> login(HttpSession session, @RequestBody Map map){
        //这里用session实现登录功能
        //log.info(map.toString());//通过map也可以进行json数据接收
        //System.out.println("进来了");
        String account = map.get("account").toString();

        String password = DigestUtils.md5DigestAsHex(map.get("password").toString().getBytes());

        System.out.println(map);
        //Object attribute = session.getAttribute(phone);
        //这个account就从redis中进行获取了
        User attribute = (User)redisTemplate.opsForValue().get(BaseContext.getId());

        if(attribute!=null && attribute.getPassword().equals(password)){
            //说明redis里面存在账号密码，登录即可

            session.setAttribute("user",attribute.getId());
            String token = JwtUtils.getJwtToken(attribute.getId(),account);//生成令牌

            BaseContext.setId(attribute.getId());//将用户id存入当前线程

            UserDto userDto = new UserDto();
            userDto.setUser(attribute);
            userDto.setToken(token);

            return R.success(userDto);
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
            redisTemplate.opsForValue().set(uu.getId(),uu);
            session.setAttribute("user",uu.getId());
            BaseContext.setId(uu.getId());

            String token = JwtUtils.getJwtToken(uu.getId(),account);//生成令牌

            UserDto userDto1 = new UserDto();
            userDto1.setUser(uu);
            userDto1.setToken(token);

            return R.success(userDto1);

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
        user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));

        userService.save(user);//mp的根据自动雪花算法会生成用户id
        //注意业务逻辑，这里注册后user表发生变化，需要更新redis

        return R.success("注册成功，请返回登录");
    }

    //根据id查找指定用户
    @GetMapping("/find/{id}")
    public User find(@PathVariable Long id){
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getId,id);

        User uu = (User)redisTemplate.opsForValue().get(id+"");
        if(uu != null){
            //说明redis中存在此用户
            return uu;
        }
        User user = userService.getOne(wrapper);
        redisTemplate.opsForValue().set(id+"",user);
        //注意这里还有一点问题
        //当用户信息发生修改的时候需要发生改变
        return user;
    }

    //用户修改功能
    @PostMapping
    public R<String>  update(@RequestBody User user){
        userService.updateById(user);
        return R.success("修改成功");
    }

    //修改用户信任值
    @PostMapping("/trust")
    public String trust(int type){
        User user= (User)redisTemplate.opsForValue().get(BaseContext.getId());
        if(user != null){
            if(type ==1){
                user.setTrustValue(user.getTrustValue() + 20);
                userService.updateById(user);
            }else {
                user.setTrustValue(user.getTrustValue() - 20);
                userService.updateById(user);
            }
            return "修改成功";
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getId,BaseContext.getId());

        User one = userService.getOne(wrapper);

        if(type ==1){
            one.setTrustValue(user.getTrustValue() + 20);
            userService.updateById(user);
        }else {
            one.setTrustValue(user.getTrustValue() - 20);
            userService.updateById(user);
        }
        redisTemplate.opsForValue().set(one.getId()+"",one);//存入redis

        return "修改成功";
    }

    //token验证,每一次页面跳转的时候进行token验证
    @GetMapping("/checkToken")
    public Boolean checkToken(HttpServletRequest request){
        String token = request.getHeader("token");

        return JwtUtils.checkToken(token);
    }

    //修改密码  也是忘记密码
    @PostMapping("/up")
    public R<String> updatePassword(@RequestBody UpdateDto userDto){
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getAccount,userDto.getAccount());

        User one = userService.getOne(wrapper);
        if(one == null){
            return R.error("对不起，没有此账号");
        }
        String number = restTemplate
                .getForObject("http://localhost:8081/person/look/"+one.getId(),String.class);

        if(number == null){
            return R.error("对不起 您的电话号为空");
        }
        if(number.equals(userDto.getNumber())){
            //如果电话号相同，修改密码即可
            one.setPassword(DigestUtils.md5DigestAsHex(userDto.getPassword().getBytes()));
            userService.updateById(one);
            return R.success("修改成功");
        }
        //电话号不相同返回错误
        return R.error("您的电话号不正确");
    }

    //kafka消费者测试
    @KafkaListener(topics = "first")
    public void consume(String msg){
        System.out.println(msg);
    }

}
