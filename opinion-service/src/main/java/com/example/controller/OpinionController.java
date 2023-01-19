package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.config.BaseContext;
import com.example.config.R;
import com.example.entity.Comment;
import com.example.entity.Opinion;
import com.example.service.OpinionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/opinion")
@CrossOrigin
public class OpinionController {

    @Autowired
    private OpinionService opinionService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    //增加舆论
    @PostMapping("/add")
    public R<String> add(@RequestBody Opinion opinion){
        //将舆论 title 和 舆论 content 和 舆论类型type 传输过来
        //要将用户id存入里面，否则会报错误
        String key = "Opinions";

        Long userId = BaseContext.getId();
        opinion.setUserId(userId);
        opinionService.save(opinion);

        //舆论数据发生更改需要删除缓存，重新查询数据库
        redisTemplate.delete(key);

        return R.success("新增成功");
    }

    //展示功能，展示舆论
    //!这里还要注意热点舆论
    @GetMapping("/list")
    public R<List<Opinion>> list(){
        //使用redis进行信息存储
        String key = "Opinions";//表示舆论key
        List<Opinion> list = (List<Opinion>)redisTemplate.opsForValue().get(key);

        if(list != null){
            //说明缓存中存在数据
            return R.success(list);
        }
        //缓存中没有数据就去数据库中查找然后存到redis即可
        List<Opinion> opinionList = opinionService.list();
        if(opinionList != null){
            redisTemplate.opsForValue().set(key,opinionList);
        }

        return R.success(opinionList);
    }

    //个人舆情信息查询
    @GetMapping("/alone")
    public R<Opinion> alone(){
        //查询单个用户舆情记录
        String key = "aloneComment";//此用户
        Long userId = BaseContext.getId();

        Opinion opinion= (Opinion) redisTemplate.opsForValue().get(key);

        if(opinion!=null){
            return R.success(opinion);
        }
        LambdaQueryWrapper<Opinion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Opinion::getUserId,userId);

        Opinion one = opinionService.getOne(wrapper);
        if(one != null){
            redisTemplate.opsForValue().set(key,one);
        }
        return R.success(one);
    }
}
