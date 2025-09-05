package com.zkk.moviedp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zkk.moviedp.dto.ShowingDTO;
import com.zkk.moviedp.entity.Showing;

import java.util.List;

public interface IShowingService extends IService<Showing> {

    List<ShowingDTO> queryByMovieIds(List<Long> movieIds);
}
