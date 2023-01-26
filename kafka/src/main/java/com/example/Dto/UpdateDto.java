package com.example.Dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UpdateDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String account;

    private String password;

    private String number;
}
