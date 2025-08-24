package com.zkk.moviedp.Interceptor;

import cn.hutool.core.util.StrUtil;
import com.zkk.moviedp.constants.JwtClaimsConstant;
import com.zkk.moviedp.dto.UserDTO;
import com.zkk.moviedp.utils.JwtProperties;
import com.zkk.moviedp.utils.JwtUtil;
import com.zkk.moviedp.utils.UserHolder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RefreshTokenInterceptor implements HandlerInterceptor {

    private final JwtProperties jwtProperties; // 直接通过构造器注入

    public RefreshTokenInterceptor(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties; // 手动接收依赖
    }

/*    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("authorization");
        log.debug(token);
        if (StrUtil.isBlank(token)) {
            return true;
        }
        //获取用户
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(LOGIN_USER_KEY + token);
        //判断用户是否存在
        if(userMap.isEmpty()) {
            return true;
        }
        //存在，保存用户信息到ThreadLocal
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap,new UserDTO(),false);
        UserHolder.saveUser(userDTO);

        stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 放行
        return true;
    }*/

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        /*// 1.获取请求头中的token
//        String token = request.getHeader("authorization");
        String token = request.getHeader(jwtProperties.getUserTokenName());
        if (StrUtil.isBlank(token)) {
            return true;
        }
        Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(),token);
        Long userId = claims.get(JwtClaimsConstant.USER_ID,Long.class);
        // 2.基于userId获取redis中的用户
        String key  = LOGIN_USER_KEY + userId;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
        // 3.判断用户是否存在
        if (userMap.isEmpty()) {
            return true;
        }
        // 4.判断token是否一致,防止有以前生成的jwt，仍然能够登录
        String jwttoken = userMap.get("jwttoken").toString();
        if(!jwttoken.equals(token)){
            return true;
        }
        // 5.将查询到的hash数据转为UserDTO
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        // 6.存在，保存用户信息到 ThreadLocal
        UserHolder.saveUser(userDTO);
        // 7.刷新token有效期
        stringRedisTemplate.expire(key, LOGIN_USER_TTL, TimeUnit.MINUTES);*/
        try {
            // 1. 获取请求头中的AccessToken
            String accessToken = request.getHeader(jwtProperties.getAccessTokenName());
            // 2. 检查AccessToken是否存在
            if (StrUtil.isBlank(accessToken)) {
                return true;
            }
            try {
                // 3. 验证AccessToken签名和过期时间
                Claims claims = JwtUtil.parseJWT(jwtProperties.getAccessTokenSecret(), accessToken);
                // 4. 提取用户信息
                Long userId = claims.get(JwtClaimsConstant.USER_ID, Long.class);
                String nickname = claims.get(JwtClaimsConstant.NICKNAME, String.class);
                String icon = claims.get(JwtClaimsConstant.ICON, String.class);
                // 5. 创建用户DTO并保存到ThreadLocal
                UserDTO userDTO = new UserDTO(userId, nickname, icon);
                UserHolder.saveUser(userDTO);
                return true; // AccessToken有效，直接放行
            } catch (ExpiredJwtException e) {
                // AccessToken过期，继续处理RefreshToken
            } catch (Exception e) {
                // AccessToken无效，拒绝访问
                return true;
            }
            // 6. 获取请求头中的RefreshToken
            String refreshToken = request.getHeader(jwtProperties.getRefreshTokenName());
            // 7. 检查RefreshToken是否存在
            if (StrUtil.isBlank(refreshToken)) {
                return true; // 没有提供任何Token，可能是公开接口，由后续认证逻辑处理
            }
            try {
                // 8. 验证RefreshToken签名和过期时间
                Claims claims = JwtUtil.parseJWT(jwtProperties.getRefreshTokenSecret(), refreshToken);
                // 9. 提取用户信息
                Long userId = claims.get(JwtClaimsConstant.USER_ID, Long.class);
                String nickname = claims.get(JwtClaimsConstant.NICKNAME, String.class);
                String icon = claims.get(JwtClaimsConstant.ICON, String.class);
                // 10. 创建新的AccessToken
                Map<String, Object> claimsMap = new HashMap<>();
                claimsMap.put(JwtClaimsConstant.USER_ID, userId);
                claimsMap.put(JwtClaimsConstant.NICKNAME, nickname);
                claimsMap.put(JwtClaimsConstant.ICON, icon);
                String newAccessToken = JwtUtil.createJWT(
                        jwtProperties.getAccessTokenSecret(),
                        jwtProperties.getAccessTokenExpireTime(),
                        claimsMap
                );
                // 11. 将新的AccessToken放入响应头
                response.setHeader(jwtProperties.getAccessTokenName(), newAccessToken);
                // 12. 创建用户DTO并保存到ThreadLocal
                UserDTO userDTO = new UserDTO(userId, nickname, icon);
                UserHolder.saveUser(userDTO);
                return true; // RefreshToken有效，生成新的AccessToken并放行
            } catch (Exception e) {
                // RefreshToken无效，拒绝访问
                return true;
            }
        } catch (Exception e) {
            // 处理未知异常
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
