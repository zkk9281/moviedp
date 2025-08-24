package com.zkk.moviedp.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
public class JwtProperties {

    // AccessToken配置
    private String accessTokenName = "Authorization";
    private String accessTokenSecret = "ThisIsA32BytesLongSecretKeyForHS256";
    private long accessTokenExpireTime = 15 * 60 * 1000; // 15分钟

    // RefreshToken配置
    private String refreshTokenName = "Refresh-Token";
    private String refreshTokenSecret = "ThisIsA32BytesLongSecretKeyForHS256";
    private long refreshTokenExpireTime = 7 * 24 * 60 * 60 * 1000; // 7天

}
