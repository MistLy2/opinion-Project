package com.example.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class Person implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;//用户个人信息id

    private Long UserId;//用户id

    private String nickname;//用户姓名

    private String gender;//用户性别

    private String number;//用户手机号

}
