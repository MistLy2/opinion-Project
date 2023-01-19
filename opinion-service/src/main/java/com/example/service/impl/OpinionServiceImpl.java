package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.Opinion;
import com.example.mapper.OpinionMapper;
import com.example.service.OpinionService;
import org.springframework.stereotype.Service;

@Service
public class OpinionServiceImpl extends ServiceImpl<OpinionMapper, Opinion> implements OpinionService {
}
