package com.example.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class R<T> implements Serializable {
    //前后端数据统一
    private Integer code; //编码：1成功，0和其它数字为失败

    private String msg;//错误信息

    private T data;//固定实体类数据，将此类序列化为json即可

    public static <T> R<T> success(T object) {
        R<T> r = new R<T>(); //将T设置为R类型然后返回
        r.data = object;
        r.code = 1;
        return r;
    }

    public static <T> R<T> error(String msg) {
        R r = new R(); //将错误信息封装为R类型，然后返回
        r.msg = msg;
        r.code = 0;
        return r;
    }
}
