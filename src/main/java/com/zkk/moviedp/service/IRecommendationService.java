package com.zkk.moviedp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zkk.moviedp.dto.RecommendedMovie;
import com.zkk.moviedp.entity.Recommendation;

import java.util.List;

public interface IRecommendationService extends IService<Recommendation> {
    /**
     * 通过用户id获取数据库中的电影推荐列表
     * @return 电影推荐列表
     */
    List<RecommendedMovie> getRecommendedMoviesByUserId();

    /**
     * 更新指定用户推荐结果
     * @param uid 用户id
     */
    void updateRecommendation(Long uid);
}
