package com.zkk.moviedp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_movie")
public class Movie {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 电影名称
     */
    private String name;

    private String directors;

    private String writers;

    /**
     * 导演
     */
    private String actors;

    /**
     * 类型, 以,隔开
     */
    private String type;

    /**
     * 地址
     */
    private String region;

    private String language;

    private String releaseDate;

    private String runtime;
    /**
     * 评分，1~5分，乘10保存，避免小数
     */
    private Integer score;

    /**
     * 评论数量
     */
    private Integer num;

    private String introduction;


    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
