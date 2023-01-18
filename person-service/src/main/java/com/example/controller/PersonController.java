package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.config.BaseContext;
import com.example.config.R;
import com.example.entity.Person;
import com.example.service.PersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequestMapping("/person")
@RestController
@Configuration
@CrossOrigin
public class PersonController {

    @Autowired
    private PersonService personService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    //用户信息展示功能
    @GetMapping("/show")
    public R<Person>  show(){
        //直接利用ThreadLocal 进行用户id的获取并且展示即可
        Long userId = BaseContext.getId();//能到当前界面肯定是已经登录了的

        //先去查询redis有没有当前数据
        Person person = (Person)redisTemplate.opsForValue().get(userId + "person");
        if(person != null){
            return R.success(person);
        }
        //然后和数据库进行联合查询
        LambdaQueryWrapper<Person> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Person::getUserId,userId);

        Person one = personService.getOne(wrapper);
        //查询到后存入redis缓存
        redisTemplate.opsForValue().set(userId+"person",one);
        return R.success(one);
    }
}
