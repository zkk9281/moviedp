package com.zkk.moviedp.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zkk.moviedp.dto.Result;
import com.zkk.moviedp.entity.Movie;
import com.zkk.moviedp.service.IMovieService;
import com.zkk.moviedp.service.IRecommendationService;
import com.zkk.moviedp.constants.SystemConstants;
import com.zkk.moviedp.utils.UserHolder;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

@Slf4j
@RestController
@RequestMapping("/movie")
public class MovieController {

    @Autowired
    public IMovieService movieService;

    /**
     * 根据id查询电影信息
     * @param id 电影id
     * @return 电影详情数据
     */
    @GetMapping("/{id}")
    @Tool
    public Result queryMovieById(@PathVariable("id") Long id) {
        try {
            log.info("调用queryMovieById工具");
            return movieService.queryById(id);
        } catch (InterruptedException e) {
            log.error(Arrays.toString(e.getStackTrace()));
            return Result.fail("服务器异常");
        }

    }

    /**
     * 新增电影信息
     * @param movie 电影数据
     * @return 电影id
     */
    @PostMapping
    public Result saveMovie(@RequestBody Movie movie) {
        // 写入数据库
        movieService.save(movie);
        // 返回店铺id
        return Result.ok(movie.getId());
    }

    /**
     * 更新电影信息
     * @param movie 电影数据
     * @return 无
     */
    @PutMapping
    public Result updateMovie(@RequestBody Movie movie) {
        return movieService.update(movie);
    }

    /**
     * 根据电影类型分页查询电影信息
     * @param typeId 电影类型
     * @param current 页码
     * @return 电影列表
     */
//    @GetMapping("/of/type")
//    public Result queryMovieByType(
//            @RequestParam("typeId") Integer typeId,
//            @RequestParam(value = "current", defaultValue = "1") Integer current,
//            @RequestParam(value = "x", required = false) Double x,
//            @RequestParam(value = "y", required = false) Double y
//    ) {
//        // 根据类型分页查询
//        return movieService.queryMovieByType(typeId, current, x, y);
//    }

    /**
     * 根据电影名称关键字分页查询电影信息
     * @param name 电影名称关键字
     * @param current 页码
     * @return 电影列表
     */
    @GetMapping("/of/name")
    @Tool(name="query_movie_by_name", value = "If you query movie information based on the movie name, the returned result is a list of movies. Use the tool to get the results first, and if you don't get the relevant content, you can retrieve it in the vector store.")
    public Result queryMovieByName(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        // 根据类型分页查询
        Page<Movie> page = movieService.query()
                .like(StrUtil.isNotBlank(name), "name", name)
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 返回数据
        log.info("调用queryMovieByName工具");
        return Result.ok(page.getRecords());
    }
}
