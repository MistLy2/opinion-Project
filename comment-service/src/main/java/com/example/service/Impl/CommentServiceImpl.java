package com.example.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.Comment;
import com.example.mapper.CommentMapper;
import com.example.service.CommentService;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
}
