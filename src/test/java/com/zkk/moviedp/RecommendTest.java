package com.zkk.moviedp;

import com.zkk.moviedp.dto.RecommendedMovie;
import com.zkk.moviedp.service.IRecommendationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class RecommendTest {

    @Autowired
    private IRecommendationService recommendationService;

    @Test
    public void testR() {
        recommendationService.updateRecommendation(1l);
    }
}
