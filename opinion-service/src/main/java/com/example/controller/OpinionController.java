package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.config.BaseContext;
import com.example.config.R;
import com.example.conn.Tess4jClient;
import com.example.entity.Liked;
import com.example.entity.Opinion;
import com.example.service.OpinionService;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/opinion")
@CrossOrigin
//使用kafka减小并发写情况下压力骤增问题
public class OpinionController {

    @Value("${opinion-service.path}")
    private String basePath;

    @Autowired
    private OpinionService opinionService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private KafkaTemplate<String,Object> kafkaTemplate;

    @Autowired
    private DefaultRedisScript<Boolean> defaultRedisScript;


    //增加舆论
    @PostMapping("/add")//这里注意还要进行审核，管理端手动将状态设置才可以查看
    //这里数据库中默认创建state是1，所有当为0时候才可以进行查看
    public R<String> add(@RequestBody Opinion opinion){
        //将舆论 title 和 舆论 content 和 舆论类型type 传输过来
        //要将用户id存入里面，否则会报错误
        String key = "Opinions";

        Long userId = BaseContext.getId();
        opinion.setUserId(userId);
        //这里可以先将消息存入kafka，然后缓慢写入数据库
        //opinionService.save(opinion);
        kafkaTemplate.send("addOpinion",opinion);

        //舆论数据发生更改需要删除缓存，重新查询数据库
        redisTemplate.delete(key);

        return R.success("新增成功");
    }
    @KafkaListener(topics = "addOpinion")
    public void addOpinion(Opinion opinion){
        opinionService.save(opinion);
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
    //多次使用，抽取出来避免冗余代码（指定舆论信息查询）
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

        //这里也可以存入kafka，然后缓慢修改数据库，减小并发压力
        //opinionService.updateById(opinion);
        kafkaTemplate.send("updateOpinionState",opinion);

        //此时应该删除redis中存有的舆论数据
        String key1 = opinionId+"OneOpinion";
        redisTemplate.delete(key1);

        return R.success("审核通过");
    }
    @KafkaListener(topics = "updateOpinionState")
    public void updateOpinionState(Opinion opinion){
        opinionService.updateById(opinion);
        String key1 = opinion.getId()+"OneOpinion";
        redisTemplate.delete(key1);
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

    //对于前10 的评判
    @PutMapping("/judge/{type}")//1表示通过，2表示没有通过，也就为假
    public R<String> judge(@PathVariable int type,Long opinionId){
        String key = opinionId+"OneOpinion";
        System.out.println("舆论id  "+opinionId);

        Opinion opinion = (Opinion)redisTemplate.opsForValue().get(key);

        if(opinion != null){
            //表示当前redis中有数据
            opinion.setJudge(type);//设置评判后注意要进行信任值增减

            opinionService.updateById(opinion);

            //增减信任值
            String s = restTemplate.postForObject("http://localhost:8080/trust", type, String.class);

            return R.success(s);
        }
        LambdaQueryWrapper<Opinion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Opinion::getId,opinionId);

        Opinion one = opinionService.getOne(wrapper);
        one.setJudge(type);

        opinionService.updateById(one);

        String s = restTemplate.postForObject("http://localhost:8080/trust", type, String.class);

        return R.success(s);
    }

    //点赞功能实现  使用redis和lua脚本
   @PutMapping("/like")
    public R<String> like(Long opinionId){
       System.out.println(opinionId);
       //System.out.println("进来了");

       List<String> keys = new ArrayList<>();
       keys.add(buildUserRedisKey(23L));
       keys.add(buildOpinionRedisKey(opinionId));

       int value=1;

      Boolean isTrue=(Boolean) redisTemplate.execute(defaultRedisScript,keys,value+"");
       //System.out.println("到这里了");

       if(isTrue){
           return R.success("点赞成功");
       }else{
           return R.success("点赞失败");
       }
   }
    private String buildUserRedisKey(Long userId) {
        return "userId" + userId;
    }

    private String buildOpinionRedisKey(Long opinionId) {
        return "opinionId" + opinionId;
    }

    //展示点赞数量
    @GetMapping("/showLike/{opinionId}")
    public R<Object> showLike(@PathVariable Long opinionId){
        //查询redis，获取当前点赞数量
        System.out.println(opinionId);
         Object o = redisTemplate.opsForZSet().score("sortSet", buildOpinionRedisKey(opinionId));
        System.out.println(o);
        System.out.println(redisTemplate.opsForHash().get(buildUserRedisKey(20L),buildOpinionRedisKey(opinionId)));

        return R.success(o);
    }

    //展示排行榜前十
    @GetMapping("/show10")
    public R<List<Opinion>> showTen(){
        String listKey = "sortSet";
        Long count = redisTemplate.opsForZSet().zCard(listKey);//获取最大记录号
        Set<Opinion> set = redisTemplate.opsForZSet().range(listKey,(count-10>=0)?count-10:0,count);

        List<Opinion> list = new ArrayList<>(set);

        //System.out.println(list);
        return R.success(list);
    }

    //数据库模糊匹配查找舆论消息
    @GetMapping("/search/{text}")
    public R<List<Opinion>> search(@PathVariable String text){
        //数据库中进行模糊匹配
        if(text == null){
            return R.error("查询条件不能为空");
        }
        LambdaQueryWrapper<Opinion> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(Opinion::getTitle,text);
        List<Opinion> list = opinionService.list(wrapper);

        return R.success(list);
    }
    //图片文字提取分析并查询数据库返回结果
    @GetMapping("/analyse/{pictureName}")
    public R<List<Opinion>> analyse(@PathVariable String pictureName){
        String result=null;

        byte[] bytes = goFile("111.png");
        //从byte[]转换为butteredImage
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        BufferedImage imageFile = null;
        try {
            imageFile = ImageIO.read(in);

            //识别图片的文字
            result= Tess4jClient.doOCR(imageFile);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //查询后返回
        return search(result);
    }
    public byte[] goFile(String name) {

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(basePath+name);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while (true) {
            try {
                if (!((rc = inputStream.read(buff, 0, 100)) > 0)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            byteArrayOutputStream.write(buff, 0, rc);
        }
        return byteArrayOutputStream.toByteArray();

    }

}
