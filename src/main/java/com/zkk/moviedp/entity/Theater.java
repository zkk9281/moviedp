package com.zkk.moviedp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_theater")
public class Theater {
    private Long id;
    private String name;
    private String address;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
