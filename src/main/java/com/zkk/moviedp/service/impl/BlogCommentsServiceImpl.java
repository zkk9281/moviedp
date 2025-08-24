package com.zkk.moviedp.service.impl;

import com.zkk.moviedp.entity.BlogComments;
import com.zkk.moviedp.mapper.BlogCommentsMapper;
import com.zkk.moviedp.service.IBlogCommentsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {

}
