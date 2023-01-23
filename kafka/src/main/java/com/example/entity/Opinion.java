package com.example.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.springframework.validation.FieldError;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class Opinion implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;//舆论id

    private Long userId;//用户id

    private String title;//舆论标题

    private String content;//舆论内容

    private String source;//舆论来源

    private String opinionType;//舆论类型

    private int trueNumber;//认为真 人数

    private int falseNumber;//认为假 人数

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;//创建时间c

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;//更新时间

    private int state;//舆论状态

    private int Judge;
}
