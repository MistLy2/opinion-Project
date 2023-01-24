package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.Liked;
import com.example.mapper.LikedMapper;
import com.example.service.LikedService;
import org.springframework.stereotype.Service;

@Service
public class LikedServiceImpl extends ServiceImpl<LikedMapper, Liked> implements LikedService {
}
