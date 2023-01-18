package com.example.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class Good implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;//商品id

    private String goodName;//商品名称

    private int scoreNumber;//兑换该商品需要的积分
}
