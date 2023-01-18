package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.entity.Good;
import com.example.service.GoodService;
import com.example.config.BaseContext;
import com.example.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import com.example.config.R;

@Slf4j
@RequestMapping("/good")
@RestController
public class GoodController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private GoodService goodService;

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @Autowired
    private RestTemplate restTemplate;

    //pc端的展示和兑换功能，管理端的添加和删除功能


    //展示功能
    @GetMapping("/list")
    public R<List<Good>> list(){
        //利用动态key 设置redis缓存
        //这里注意如果数据发生修改，则需要删除redis中的缓存，从数据库中获取
        String key = "goods";
        kafkaTemplate.send("first","hello");
        LambdaQueryWrapper<Good> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Good::getScoreNumber);

        //先去查询redis中是否有值
        List<Good> goodsList = (List<Good>) redisTemplate.opsForValue().get(key);
        if(goodsList != null){
            //说明redis中有值，直接返回即可
            return R.success(goodsList);
        }

        List<Good> list = goodService.list(wrapper);
        redisTemplate.opsForValue().set(key,list);
        return R.success(list);
    }

    //兑换功能
    @PostMapping("/add")
    public R<String>  conversion(@RequestBody Good good){
        System.out.println(good);
        //需要传过来商品id
        //其实就是给用户兑换商品，每一次只可以兑换一次
        //每一个商品兑换后只有当前商品过期，才可以兑换下一个商品
        Long id = BaseContext.getId();
        //然后判断当前用户是否已经兑换，如果已经兑换，就不能进行
        User user =  restTemplate.getForObject("http://localhost:8080/user/find/1",User.class);
        System.out.println("到了");

        assert user != null;
        if(user.getGoodId()!=null){
            //说明已经有商品了就不能进行兑换
            return R.error("您还有商品未使用完毕");
        }
        //说明可以兑换
        //然后保存商品到user
        user.setGoodId(good.getId());
        restTemplate.postForObject("http://localhost:8080/user",user,String.class);

        return R.success("商品兑换成功");
    }

    @KafkaListener(topics = "update")
    public void consume(String msg){
        System.out.println(msg);
    }
}
