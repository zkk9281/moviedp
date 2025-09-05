package com.zkk.moviedp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zkk.moviedp.dto.ShowingDTO;
import com.zkk.moviedp.entity.Showing;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShowingMapper extends BaseMapper<Showing> {

    List<ShowingDTO> queryByMovieIds(@Param("movieIds") List<Long> movieIds);
}
