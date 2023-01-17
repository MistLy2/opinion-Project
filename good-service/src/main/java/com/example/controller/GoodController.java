package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.R;
import com.example.entity.Good;
import com.example.service.GoodService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@RequestMapping("/good")
@RestController
public class GoodController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private GoodService goodService;

    private RestTemplate restTemplate;

    //pc端的展示和兑换功能，管理端的添加和删除功能

    //展示功能
    @GetMapping("/list")
    public R<List<Good>> list(){
        //利用动态key 设置redis缓存
        //这里注意如果数据发生修改，则需要删除redis中的缓存，从数据库中获取
        String key = "goods";
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
    
}
