package com.zkk.moviedp.controller;


import cn.hutool.core.bean.BeanUtil;
import com.zkk.moviedp.dto.LoginFormDTO;
import com.zkk.moviedp.dto.Result;
import com.zkk.moviedp.dto.UserDTO;
import com.zkk.moviedp.entity.User;
//import com.zkk.moviedp.entity.UserInfo;
//import com.zkk.moviedp.service.IUserInfoService;
import com.zkk.moviedp.service.IUserService;
import com.zkk.moviedp.utils.UserHolder;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpSession;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;


    /**
     * 发送手机验证码
     */
    @PostMapping("code")
    public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
        // 发送短信验证码并保存验证码
        return userService.sendCode(phone, session);
    }


    /**
     * 登录功能
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm, HttpSession session){
        //  实现登录功能
        return userService.login(loginForm, session);
    }

    /**
     * 登出功能
     * @return 无
     */
    @PostMapping("/logout")
    public Result logout(){
        // TODO 实现登出功能
        return Result.fail("功能未完成");
    }

    @GetMapping("/me")
    public Result me(){
        // 获取当前登录的用户并返回
        UserDTO user = UserHolder.getUser();
        return Result.ok(user);
    }

//    @GetMapping("/info/{id}")
//    public Result info(@PathVariable("id") Long userId){
//        // 查询详情
//        UserInfo info = userInfoService.getById(userId);
//        if (info == null) {
//            // 没有详情，应该是第一次查看详情
//            return Result.ok();
//        }
//        info.setCreateTime(null);
//        info.setUpdateTime(null);
//        // 返回
//        return Result.ok(info);
//    }

    @GetMapping("/{id}")
    public Result queryUserById(@PathVariable("id") Long userId){
        // 查询详情
        User user = userService.getById(userId);
        if (user == null) {
            return Result.ok();
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // 返回
        return Result.ok(userDTO);
    }

    @PostMapping("/sign")
    public Result sign(){
        return userService.sign();
    }

    @GetMapping("/sign/count")
    public Result signCount(){
        return userService.signCount();
    }

}
