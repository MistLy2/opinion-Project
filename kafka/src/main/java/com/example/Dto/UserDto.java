package com.example.Dto;

import com.example.entity.User;
import lombok.Data;

@Data
public class UserDto {

    private  User user;

    private String token;
}
