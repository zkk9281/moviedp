package com.zkk.moviedp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendedMovie {
    private Long id;

    private Long did;

    private String name;

    private Double score;

    private String pic;

    private String directors;

    private String actors;

    private String regions;

    private String types;

    /**
     * 推荐指数
     */
    private Double idx;
}
