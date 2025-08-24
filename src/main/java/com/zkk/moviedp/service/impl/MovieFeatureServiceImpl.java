package com.zkk.moviedp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkk.moviedp.entity.MovieFeature;
import com.zkk.moviedp.mapper.MovieFeatureMapper;
import com.zkk.moviedp.service.IMovieFeatureService;
import org.springframework.stereotype.Service;

@Service
public class MovieFeatureServiceImpl extends ServiceImpl<MovieFeatureMapper, MovieFeature>
        implements IMovieFeatureService {
}
