package com.zkk.moviedp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zkk.moviedp.dto.LoginFormDTO;
import com.zkk.moviedp.dto.Result;
import com.zkk.moviedp.entity.User;
import jakarta.servlet.http.HttpSession;


public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);

    Result sign();

    Result signCount();
}
