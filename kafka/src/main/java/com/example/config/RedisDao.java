package com.example.config;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class RedisDao {

    @Autowired
    private RedisTemplate redisTemplate;

    public <T> T executeScript(DefaultRedisScript<T> script, List<String> keys,Object ...args){
        return (T) redisTemplate.execute(script,keys,args);
    }
}
