package com.example.config;

public class BaseContext {
    //工具类，用来封装ThreadLocal 用来获取员工id传入下一个区域,让元数据对象处理器获取到
    private static ThreadLocal<Long> threadLocal=new ThreadLocal<>();

    public static void setId(Long id){
        threadLocal.set(id);
    }

    public static Long getId(){
        return threadLocal.get();
    }
}
