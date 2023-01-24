package com.example.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class Liked implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;//主键

    private Long opinionId;//舆论id

    private Long userId;//用户id

    private int status;//点赞状态
}
