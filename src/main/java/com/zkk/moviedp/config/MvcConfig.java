package com.zkk.moviedp.config;

import com.zkk.moviedp.utils.JwtProperties;
import com.zkk.moviedp.Interceptor.LoginInterceptor;
import com.zkk.moviedp.Interceptor.RefreshTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录拦截器
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns(
                        "/user/code",
                        "/user/login",
                        "/blog/hot",
                        "/movie/**",
                        "/voucher/**",
                        "/upload/**",
                        "/chat/**",
                        "/showing/**"
                ).order(1);
        // token刷新拦截器
        registry.addInterceptor(new RefreshTokenInterceptor(jwtProperties)).addPathPatterns("/**").order(0);
    }
}
