package com.zkk.moviedp.controller;

import com.zkk.moviedp.dto.Result;
import com.zkk.moviedp.service.IShowingService;
import com.zkk.moviedp.service.ITheaterService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/showing")
public class ShowingController {

    @Autowired
    private IShowingService showingService;

    @Autowired
    private ITheaterService theaterService;

    // 根据电影id列表查询播放信息，使用ShowingDTO封装
    @PostMapping("/query")
    public Result query(@RequestBody List<Long> movieIds) {
        return Result.ok(showingService.queryByMovieIds(movieIds));
    }
}
