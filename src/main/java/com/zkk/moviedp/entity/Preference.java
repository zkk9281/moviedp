package com.zkk.moviedp.entity;

import lombok.Data;

/**
 * 记录用户偏好，用于协同过滤
 */
@Data
public class Preference {
    private Long userId;

    private Long movieId;

    private Integer score;

    private Integer cnt;
}
