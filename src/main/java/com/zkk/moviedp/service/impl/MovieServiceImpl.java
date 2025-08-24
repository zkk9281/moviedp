package com.zkk.moviedp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.zkk.moviedp.dto.Result;
import com.zkk.moviedp.entity.Movie;
import com.zkk.moviedp.mapper.MovieMapper;
import com.zkk.moviedp.service.IMovieService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkk.moviedp.utils.CacheClient;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.zkk.moviedp.constants.RedisConstants.*;

@Slf4j
@Service
public class MovieServiceImpl extends ServiceImpl<MovieMapper, Movie> implements IMovieService {

    @Autowired
    private MovieMapper movieMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private CacheClient cacheClient;
    @Autowired
    private RedissonClient RedissonClient;

    @PostConstruct
    public void initBloomFilter() {
        // 1. 从数据库加载所有店铺 ID
        List<Long> movieIds = movieMapper.selectList(null)
                .stream().map(Movie::getId).toList();

        // 2. 创建布隆过滤器
        RBloomFilter<Long> bloomFilter = RedissonClient.getBloomFilter("movieList");
        bloomFilter.tryInit(1000000L, 0.03);

        // 3. 将店铺 ID 添加到布隆过滤器
        for (Long movieId : movieIds) {
            bloomFilter.add(movieId);
        }
    }

    @Override
    public Result queryById(Long id) throws InterruptedException {
        /* // 缓存空对象解决缓存穿透
        Movie movie = cacheClient
                .queryWithPassThrough(CACHE_MOVIE_KEY, id, Movie.class, this::getById, CACHE_MOVIE_TTL, TimeUnit.MINUTES);
        */
        // 布隆过滤器解决缓存穿透
        if (!RedissonClient.getBloomFilter("movieList").contains(id)) {
            // 如果不在，说明数据库中肯定不存在，直接返回错误
            return Result.fail("布隆拦截电影不存在");
        }

        // 互斥锁解决缓存击穿
        String key = CACHE_MOVIE_KEY + id;
        // 1.从redis查询缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        log.debug("从redis查询电影缓存：{}", json);
        // 2.如果存在直接返回
        if (StrUtil.isNotBlank(json)) {
            return Result.ok(JSONUtil.toBean(json, Movie.class));
        }
        // 3.不存在获取锁更新缓存
        RLock lock = RedissonClient.getLock(LOCK_MOVIE_KEY + id);
        boolean isLock = lock.tryLock(1, 10, TimeUnit.SECONDS);
        // 4.获取锁成功则查询数据库更新缓存
        if (isLock) {
            try {
                // 双重检查锁
                String json2 = stringRedisTemplate.opsForValue().get(key);
                if (StrUtil.isNotBlank(json2)) {
                    return Result.ok(JSONUtil.toBean(json2, Movie.class));
                }
                Movie movie = getById(id);
                if (movie == null) {
                    return Result.fail("电影不存在");
                }
                cacheClient.set(key, movie, CACHE_MOVIE_TTL, TimeUnit.MINUTES);
                return Result.ok(movie);
            } finally {
                lock.unlock();
            }
        }
        // 5.获取不成功就等待并重试
        int retryTimes = 3;
        String movieJson = null;
        while(StrUtil.isBlank(movieJson) && retryTimes > 0) {
            // 休眠300ms后递归
            TimeUnit.MILLISECONDS.sleep(300L);
            movieJson = stringRedisTemplate.opsForValue().get(key);
            retryTimes--;
        }
        if(StrUtil.isNotBlank(movieJson)) {
            return Result.ok(JSONUtil.toBean(json, Movie.class));
        }
        return Result.fail("电影不存在");
        // 逻辑过期解决缓存击穿
        /*Movie movie = cacheClient
                .queryWithLogicalExpire(CACHE_MOVIE_KEY, id, Movie.class, this::getById, CACHE_MOVIE_TTL, TimeUnit.MINUTES);
*/
//        if(movie == null) {
//            return Result.fail("电影不存在");
//        }
//        return Result.ok(movie);
    }

//    private Movie queryWithMutex(Long id) {
//        //从redis查询缓存
//        String movieJson = stringRedisTemplate.opsForValue().get(CACHE_MOVIE_KEY + id);
//        //存在直接返回
//        if (StrUtil.isNotBlank(movieJson)) {
//            return JSONUtil.toBean(movieJson, Movie.class);
//        }
//        //判断命中的是否是空值
//        if (movieJson != null) {
//            return null;
//        }
//
//        Movie movie = null;
//        try {//互斥锁防止缓存击穿
//            boolean isLock = tryLock(LOCK_MOVIE_KEY + id);
//
//            if (!isLock) {
//                Thread.sleep(50);
//                return queryWithMutex(id);
//            }
//
//            //再次从redis查询缓存
//            movieJson = stringRedisTemplate.opsForValue().get(CACHE_MOVIE_KEY + id);
//            //存在直接返回
//            if (StrUtil.isNotBlank(movieJson)) {
//                return JSONUtil.toBean(movieJson, Movie.class);
//            }
//            //判断命中的是否是空值
//            if (movieJson != null) {
//                return null;
//            }
//
//            //不存在查询数据库
//            movie = getById(id);
//            //不存在返回错误
//            if (movie == null) {
//                stringRedisTemplate.opsForValue().set(CACHE_MOVIE_KEY + id, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
//                return null;
//            }
//            //存在写入redis，返回
//            stringRedisTemplate.opsForValue().set(CACHE_MOVIE_KEY + id, JSONUtil.toJsonStr(movie), CACHE_MOVIE_TTL, TimeUnit.MINUTES);
//        } catch (InterruptedException e){
//            throw new RuntimeException(e);
//        } finally {
//            unlock(LOCK_MOVIE_KEY + id);
//        }
//
//        return movie;
//    }

//    private Movie queryWithPassThrough(Long id) {
//        //从redis查询缓存
//        String movieJson = stringRedisTemplate.opsForValue().get(CACHE_MOVIE_KEY + id);
//        //存在直接返回
//        if (StrUtil.isNotBlank(movieJson)) {
//            return JSONUtil.toBean(movieJson, Movie.class);
//        }
//        //判断命中的是否是空值
//        if (movieJson != null) {
//            return null;
//        }
//
//        //不存在查询数据库
//        Movie movie = getById(id);
//        //不存在返回错误
//        if(movie == null) {
//            stringRedisTemplate.opsForValue().set(CACHE_MOVIE_KEY + id, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
//            return null;
//        }
//        //存在写入redis，返回
//        stringRedisTemplate.opsForValue().set(CACHE_MOVIE_KEY + id, JSONUtil.toJsonStr(movie), CACHE_MOVIE_TTL, TimeUnit.MINUTES);
//
//        return movie;
//    }

//    private boolean tryLock(String key) {
//        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", LOCK_MOVIE_TTL, TimeUnit.SECONDS);
//
//        return BooleanUtil.isTrue(flag);//拆箱
//    }
//
//    private void unlock(String key) {
//        stringRedisTemplate.delete(key);
//    }

    @Override
    @Transactional
    public Result update(Movie movie) {
        Long id = movie.getId();
        if (id == null) {
            return Result.fail("id不能为空");
        }
        // 更新数据库
        updateById(movie);
        // 删除缓存
        stringRedisTemplate.delete(CACHE_MOVIE_KEY + id);

        return Result.ok();
    }

//    @Override
//    public Result queryMovieByType(Integer typeId, Integer current, Double x, Double y) {
//        // 1.判断是否需要根据坐标查询
//        if (x == null || y == null) {
//            // 不需要坐标查询，按数据库查询
//            Page<Movie> page = query()
//                    .eq("type_id", typeId)
//                    .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
//            // 返回数据
//            return Result.ok(page.getRecords());
//        }
//
//        // 2.计算分页参数
//        int from = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
//        int end = current * SystemConstants.DEFAULT_PAGE_SIZE;
//
//        // 3.查询redis、按照距离排序、分页。结果：movieId、distance
//        String key = MOVIE_GEO_KEY + typeId;
//        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo() // GEOSEARCH key BYLONLAT x y BYRADIUS 10 WITHDISTANCE
//                .search(
//                        key,
//                        GeoReference.fromCoordinate(x, y),
//                        new Distance(5000),
//                        RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().limit(end)
//                );
//        // 4.解析出id
//        if (results == null) {
//            return Result.ok(Collections.emptyList());
//        }
//        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
//        if (list.size() <= from) {
//            // 没有下一页了，结束
//            return Result.ok(Collections.emptyList());
//        }
//        // 4.1.截取 from ~ end的部分
//        List<Long> ids = new ArrayList<>(list.size());
//        Map<String, Distance> distanceMap = new HashMap<>(list.size());
//        list.stream().skip(from).forEach(result -> {
//            // 4.2.获取店铺id
//            String movieIdStr = result.getContent().getName();
//            ids.add(Long.valueOf(movieIdStr));
//            // 4.3.获取距离
//            Distance distance = result.getDistance();
//            distanceMap.put(movieIdStr, distance);
//        });
//        // 5.根据id查询Movie
//        String idStr = StrUtil.join(",", ids);
//        List<Movie> movies = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();
//        for (Movie movie : movies) {
//            movie.setDistance(distanceMap.get(movie.getId().toString()).getValue());
//        }
//        // 6.返回
//        return Result.ok(movies);
//        return Result.ok();
//    }
}
