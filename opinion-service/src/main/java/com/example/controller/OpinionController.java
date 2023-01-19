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
    @PostMapping("/add")//这里注意还要进行审核，管理端手动将状态设置才可以查看
    //这里数据库中默认创建state是1，所有当为0时候才可以进行查看
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

    //展示功能，展示舆论,当前查询是管理端查询，可以查出来所有state为1的，也就是还没有审核通过的
    //!这里还要注意热点舆论
    @GetMapping("/list1")
    public R<List<Opinion>> list1(){
        //使用redis进行信息存储
        String key = 1+"Opinions";//表示还没有审核通过的舆论key
        List<Opinion> list = (List<Opinion>)redisTemplate.opsForValue().get(key);

        if(list != null){
            //说明缓存中存在数据
            return R.success(list);
        }
        //缓存中没有数据就去数据库中查找然后存到redis即可
        LambdaQueryWrapper<Opinion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Opinion::getState,1);
        List<Opinion> opinionList = opinionService.list(wrapper);
        if(opinionList != null){
            redisTemplate.opsForValue().set(key,opinionList);
        }

        return R.success(opinionList);
    }

    //个人舆情信息查询
    @GetMapping("/alone")
    public R<List<Opinion>> alone(){
        //查询单个用户舆情记录,多个记录
        Long userId = BaseContext.getId();
        String key = userId+"aloneComment";//此用户,也必须是单独的，否则数据错乱

        List<Opinion> list= (List<Opinion>) redisTemplate.opsForValue().get(key);

        if(list!=null){
            return R.success(list);
        }
        LambdaQueryWrapper<Opinion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Opinion::getUserId,userId);

        List<Opinion> opinionList = opinionService.list(wrapper);
        if(opinionList != null){
            redisTemplate.opsForValue().set(key,opinionList);
        }
        return R.success(opinionList);
    }

    //指定舆情信息查询
    @GetMapping("/assign")//前端给我传舆情id，进行查询
    //然后前端做一个事件绑定机制，用户点击查询舆论信息
    public R<Opinion> assign(Long opinionId){
        Opinion one = assigned(opinionId);
        return R.success(one);
    }
    //多次使用，抽取出来避免冗余代码
    public Opinion assigned(Long opinionId){
        String key = opinionId+"OneOpinion";//单个舆论,并且不能重复,用id表示

        Opinion opinion = (Opinion) redisTemplate.opsForValue().get(key);
        if(opinion != null){
            //说明redis中有数据，返回即可
            return opinion;
        }

        LambdaQueryWrapper<Opinion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Opinion::getId,opinionId);

        Opinion one = opinionService.getOne(wrapper);
        redisTemplate.opsForValue().set(key,one);

        return one;
    }

    //审核通过功能
    @PostMapping("/update")
    //其实就是修改，审核通过后将state设置为 0 即可
    public R<String> update(Long opinionId){
        Opinion opinion = assigned(opinionId);//查询指定舆论
        opinion.setState(0);//表示审核通过

        opinionService.updateById(opinion);

        //此时应该删除redis中存有的舆论数据
        String key1 = opinionId+"OneOpinion";
        redisTemplate.delete(key1);

        return R.success("审核通过");
    }


    @GetMapping("/list0")
    //当前方法只查询审核通过的舆论
    public R<List<Opinion>> list0(){
        //使用redis进行信息存储
        String key = 0+"Opinions";//表示还没有审核通过的舆论key
        List<Opinion> list = (List<Opinion>)redisTemplate.opsForValue().get(key);

        if(list != null){
            //说明缓存中存在数据
            return R.success(list);
        }
        //缓存中没有数据就去数据库中查找然后存到redis即可
        LambdaQueryWrapper<Opinion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Opinion::getState,0);
        List<Opinion> opinionList = opinionService.list(wrapper);
        if(opinionList != null){
            redisTemplate.opsForValue().set(key,opinionList);
        }

        return R.success(opinionList);
    }
}
