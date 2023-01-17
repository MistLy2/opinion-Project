package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class MessageController {

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;//通过当前数据发送到指定集群

    @GetMapping(value = "/sendMessage/{msg}")
    public String sendMessage(@PathVariable("msg") String msg){
       kafkaTemplate.send("test1",msg);

       return "ok";
    }

}
