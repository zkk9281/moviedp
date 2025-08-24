package com.zkk.moviedp.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkk.moviedp.dto.LoginFormDTO;
import com.zkk.moviedp.dto.Result;
import com.zkk.moviedp.entity.User;
import com.zkk.moviedp.mapper.UserMapper;
import com.zkk.moviedp.utils.JwtProperties;
import com.zkk.moviedp.service.IUserService;
import com.zkk.moviedp.constants.JwtClaimsConstant;
import com.zkk.moviedp.utils.JwtUtil;
import com.zkk.moviedp.utils.RegexUtils;
import com.zkk.moviedp.utils.UserHolder;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.zkk.moviedp.constants.RedisConstants.*;
import static com.zkk.moviedp.constants.SystemConstants.USER_NICK_NAME_PREFIX;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        //校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            //不符合就反回错误
            return Result.fail("手机号格式错误");
        }

        //符合，生成验证码
        String code = RandomUtil.randomNumbers(6);
        //保存到session
//        session.setAttribute("code", code);
        // 保存到redis
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        // 更新发送时间和次数
        stringRedisTemplate.opsForZSet().add(SENDCODE_SENDTIME_KEY + phone, System.currentTimeMillis() + "", System.currentTimeMillis());
        //发送验证码
        log.debug("发送验证码成功，验证码：" + code);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        //校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            //不符合就反回错误
            return Result.fail("手机号格式错误");
        }
        //校验验证码
//        Object cacheCode = session.getAttribute("code");
//        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
//        String code = loginForm.getCode();
//        if(cacheCode == null || !cacheCode.equals(code)){
//            return Result.fail("验证码错误");
//        }
        //不一致就报错

        //一致就根据手机号查询用户
        User user = query().eq("phone", phone).one();
        //不存在就创建新用户保存
        if(user == null) {
            user = createUserWithPhone(phone);
        }

/*        // 6.生成JWT
        Map<String,Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID,user.getId());
//        String token = JwtUtil.createJWT(jwtProperties.getSecretKey(),jwtProperties.getTtl(),claims);
//        String jwttoken = JwtUtil.createJWT("ThisIsA32BytesLongSecretKeyForHS256",30*60*1000,claims);
        String jwttoken = JwtUtil.createJWT(jwtProperties.getUserSecretKey(),jwtProperties.getUserTtl(),claims);


        //保存用户信息到session
//        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
//        String token = UUID.randomUUID().toString(true);
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
//        stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY + token, userMap);
//        stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.MINUTES);*/
        // 生成JWT双Token
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        claims.put(JwtClaimsConstant.NICKNAME, user.getNickName());
        claims.put(JwtClaimsConstant.ICON, user.getIcon());
        // 生成AccessToken (有效期较短)
        String accessToken = JwtUtil.createJWT(
                jwtProperties.getAccessTokenSecret(),
                jwtProperties.getAccessTokenExpireTime(),
                claims// 15分钟
        );
        // 生成RefreshToken (有效期较长)
        String refreshToken = JwtUtil.createJWT(
                jwtProperties.getRefreshTokenSecret(),
                jwtProperties.getRefreshTokenExpireTime(),
                claims // 7天
        );

        /*// 7.3.存储
        String tokenKey = LOGIN_USER_KEY + userDTO.getId();
        // 7.4.将jwttoken存入userMap中
        userMap.put("jwttoken",jwttoken);
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 7.5.设置redis中 userId的有效期
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);*/

        // 返回双Token给客户端
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return Result.ok(tokens);
    }

    @Override
    public Result sign() {
        // 1.获取当前登录用户
        Long userId = UserHolder.getUser().getId();
        // 2.获取日期
        LocalDateTime now = LocalDateTime.now();
        // 3.拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY + userId + keySuffix;
        // 4.获取今天是本月的第几天
        int dayOfMonth = now.getDayOfMonth();
        // 5.写入Redis SETBIT key offset 1
        stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
        return Result.ok();
    }

    @Override
    public Result signCount() {
        // 1.获取当前登录用户
        Long userId = UserHolder.getUser().getId();
        // 2.获取日期
        LocalDateTime now = LocalDateTime.now();
        // 3.拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY + userId + keySuffix;
        // 4.获取今天是本月的第几天
        int dayOfMonth = now.getDayOfMonth();
        // 5.获取本月截止今天为止的所有的签到记录，返回的是一个十进制的数字 BITFIELD sign:5:202203 GET u14 0
        List<Long> result = stringRedisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0)
        );
        if (result == null || result.isEmpty()) {
            // 没有任何签到结果
            return Result.ok(0);
        }
        Long num = result.get(0);
        if (num == null || num == 0) {
            return Result.ok(0);
        }
        // 6.循环遍历
        int count = 0;
        while (true) {
            // 6.1.让这个数字与1做与运算，得到数字的最后一个bit位  // 判断这个bit位是否为0
            if ((num & 1) == 0) {
                // 如果为0，说明未签到，结束
                break;
            }else {
                // 如果不为0，说明已签到，计数器+1
                count++;
            }
            // 把数字右移一位，抛弃最后一个bit位，继续下一个bit位
            num >>>= 1;
        }
        return Result.ok(count);
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        save(user);
        return user;
    }
}
