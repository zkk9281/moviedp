package com.zkk.moviedp.mapper;

import com.zkk.moviedp.entity.Blog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zkk.moviedp.entity.Preference;

import java.util.List;

public interface BlogMapper extends BaseMapper<Blog> {
    /**
     * 获取用户所有的偏好
     */
    List<Preference> selectAllPreferences();
}
