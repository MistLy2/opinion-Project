package com.example.config;

public class CustomException extends RuntimeException{
    //定义业务异常类
    public CustomException(String message){
        super(message);
    }
}
