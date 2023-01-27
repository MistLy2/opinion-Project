package com.example.controller;

import com.example.config.BaseContext;
import com.example.config.R;
import com.example.entity.Comment;
import com.example.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@RequestMapping("/comment")
@RestController
@CrossOrigin//处理服务跳转
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    //添加评论
    @PostMapping("/add")
    public R<String> add(@RequestBody Comment comment){
        //评论表发生变化时候，需要改变redis缓存
        String key = "commentList";

        Long userId = BaseContext.getId();
        comment.setUserId(userId);
        commentService.save(comment);

        redisTemplate.delete(key);//删除下一次重新查询数据库

        return R.success("添加成功");
    }


    //评论查询功能
    @GetMapping("/list")
    public R<List<Comment>> list(){
        //System.out.println("进来了");

        String key = "commentList";
        List<Comment> commentList = (List<Comment>) redisTemplate.opsForValue().get(key);

        if(commentList != null){
            //说明存在数据，返回即可
            return R.success(commentList);
        }

        List<Comment> list = commentService.list();
        if(list != null){  //防止空指针
            //存入redis
            redisTemplate.opsForValue().set(key,list);
        }
        return R.success(list);
    }
}
