package com.zkk.moviedp.controller;

import com.zkk.moviedp.dto.Result;
import com.zkk.moviedp.service.IRecommendationService;
import com.zkk.moviedp.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recommendation")
public class RecommendationController {

    @Autowired
    public IRecommendationService recommendationService;

    /**
     * @return 推荐列表
     */
    @GetMapping("")
    public Result getRecommendation() {
        return Result.ok(recommendationService.getRecommendedMoviesByUserId());
    }
}
