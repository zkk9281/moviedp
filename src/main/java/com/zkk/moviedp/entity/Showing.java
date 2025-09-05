package com.zkk.moviedp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_showing")
public class Showing {
    private Long id;
    private Long movieId;
    private Long theaterId;
    private Double price;
    private String startTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
