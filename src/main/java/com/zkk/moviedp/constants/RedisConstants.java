package com.zkk.moviedp.constants;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 30L;

    public static final Long CACHE_NULL_TTL = 2L;

    public static final Long CACHE_MOVIE_TTL = 30L;
    public static final String CACHE_MOVIE_KEY = "cache:movie:";

    public static final String CACHE_MOVIE_TYPE_KEY = "cache:movie-type";

    public static final String LOCK_MOVIE_KEY = "lock:movie:";
    public static final Long LOCK_MOVIE_TTL = 10L;

    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
    public static final String BLOG_LIKED_KEY = "blog:liked:";
    public static final String FEED_KEY = "feed:";
    public static final String MOVIE_GEO_KEY = "movie:geo:";
    public static final String USER_SIGN_KEY = "sign:";

    public static final String SENDCODE_SENDTIME_KEY ="sms:sendtime:";
    public static final String ONE_LEVERLIMIT_KEY ="limit:onelevel:";
    public static final String TWO_LEVERLIMIT_KEY ="limit:twolevel:";
}
