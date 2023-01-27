package com.example.conn;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.entity.Opinion;
import com.example.service.LikedService;
import com.example.service.OpinionService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.LinkedHashSet;
import java.util.zip.DeflaterOutputStream;

//定时任务
@Slf4j
public class CronUtil extends QuartzJobBean {
    @Autowired
    private OpinionService opinionService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private LikedService likedService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        //定时将redis中点赞数量数据存入数据库，防止丢失
        flush();
    }
    public void flush(){
        //int count = Math.toIntExact(redisTemplate.opsForZSet().count("sortSet", 0, -1));
        LinkedHashSet<String> list= (LinkedHashSet<String>) redisTemplate.opsForZSet().range("sortSet",0,-1);

        for (String s : list){
            String substring = s.substring(9);
            System.out.println(substring);
            Long opinionId = Long.parseLong(substring);
            double count = redisTemplate.opsForZSet().score("sortSet",s);
            //刷新数据即可
            Opinion opinion = new Opinion();
            opinion.setId(opinionId);
            opinion.setTrueNumber((int) count);

            opinionService.updateById(opinion);
        }
    }
}
