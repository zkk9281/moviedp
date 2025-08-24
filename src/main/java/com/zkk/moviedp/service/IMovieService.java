package com.zkk.moviedp.service;

import com.zkk.moviedp.dto.Result;
import com.zkk.moviedp.entity.Movie;
import com.zkk.moviedp.entity.Movie;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IMovieService extends IService<Movie> {

    Result queryById(Long id) throws InterruptedException;

    Result update(Movie movie);

    //Result queryMovieByType(Integer typeId, Integer current, Double x, Double y);

}
