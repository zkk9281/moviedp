package com.zkk.moviedp.service;

import com.zkk.moviedp.dto.Result;
import com.zkk.moviedp.entity.Follow;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IFollowService extends IService<Follow> {

    Result isFollow(Long followUserId);

    Result followCommons(Long id);

    Result follow(Long followUserId, Boolean isFollow);
}
