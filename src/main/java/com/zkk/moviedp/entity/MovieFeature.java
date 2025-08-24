package com.zkk.moviedp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@TableName(value ="tb_movie_feature")
@Data
public class MovieFeature implements Serializable {
    /**
     * 电影id
     */
    @TableId
    private Long mid;

    /**
     * 电影特征矩阵
     */
    private String matrix;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
