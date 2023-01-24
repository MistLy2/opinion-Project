package com.example.conn;

import com.example.entity.Liked;
import com.example.service.LikedService;
import com.example.service.OpinionService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

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

    }
}
