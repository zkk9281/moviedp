package com.zkk.moviedp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkk.moviedp.entity.TypeLike;
import com.zkk.moviedp.mapper.TypeLikeMapper;
import com.zkk.moviedp.service.ITypeLikeService;
import org.springframework.stereotype.Service;

@Service
public class TypeLikeServiceImpl extends ServiceImpl<TypeLikeMapper, TypeLike>
        implements ITypeLikeService {
}
