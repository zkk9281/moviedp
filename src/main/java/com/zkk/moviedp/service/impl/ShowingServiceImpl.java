package com.zkk.moviedp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkk.moviedp.dto.ShowingDTO;
import com.zkk.moviedp.entity.Showing;
import com.zkk.moviedp.mapper.ShowingMapper;
import com.zkk.moviedp.service.IShowingService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShowingServiceImpl extends ServiceImpl<ShowingMapper, Showing> implements IShowingService {

    @Override
    @Tool(name="query_showing_by_movie_ids", value = "Use this tool to query the playback information of the corresponding movie according to MovieIDS")
    public List<ShowingDTO> queryByMovieIds(List<Long> movieIds) {
        return baseMapper.queryByMovieIds(movieIds);
    }
}
