package com.zkk.moviedp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkk.moviedp.entity.Theater;
import com.zkk.moviedp.mapper.TheaterMapper;
import com.zkk.moviedp.service.ITheaterService;
import org.springframework.stereotype.Service;

@Service
public class TheaterServiceImpl extends ServiceImpl<TheaterMapper, Theater> implements ITheaterService {
}
