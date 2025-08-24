package com.zkk.moviedp;

import com.zkk.moviedp.entity.Movie;
import com.zkk.moviedp.entity.MovieFeature;
import com.zkk.moviedp.service.IMovieFeatureService;
import com.zkk.moviedp.service.IMovieService;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class MoviedpApplicationTests {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    @Test
    void contextLoads() {
    }


    @Test
    public void testRedis() throws IOException {
        stringRedisTemplate.opsForValue().set("testKey", "testValue");
        String value = stringRedisTemplate.opsForValue().get("testKey");
        System.out.println("从 Redis 获取的值: " + value); // 检查是否为 null

		RedisConnectionFactory factory = stringRedisTemplate.getConnectionFactory();
		if (factory instanceof LettuceConnectionFactory) {
			LettuceConnectionFactory lettuceFactory = (LettuceConnectionFactory) factory;
			System.out.println("实际连接的Redis主机: " + lettuceFactory.getHostName());
			System.out.println("实际连接的Redis端口: " + lettuceFactory.getPort());
		}
	}

    @Autowired
    private IMovieService movieService;

    @Autowired
    private IMovieFeatureService featureService;

    // 特征维度：类型数（需根据tb_movie_type表实际数据调整，示例中为22种类型）
    private static final int TYPE_DIMENSION = 22;
    // 根据movie表的数据生成movie feature
    @Test
    public void generateMovieFeatureMatrix() {
        // 1. 获取所有电影数据
        List<Movie> movies = movieService.list();
        if (movies.isEmpty()) {
            System.out.println("无电影数据，跳过特征生成");
            return;
        }

        // 2. 提取公共属性用于归一化计算
        List<Integer> years = extractYears(movies);
        List<Integer> runtimes = extractRuntimes(movies);
        int minYear = 1550;
        int maxYear = 2050;
        int minRuntime = 0;
        int maxRuntime = 300;

        // 3. 生成特征向量并保存
        List<MovieFeature> featureList = new ArrayList<>();
        for (Movie movie : movies) {
            try {
                // 3.1 解析基础属性
                long mid = movie.getId();
                String typeStr = movie.getType();
                String releaseDate = movie.getReleaseDate();
                String runtimeStr = movie.getRuntime();

                // 3.2 生成类型独热向量
                double[] typeVector = buildTypeOneHotVector(typeStr);

                // 3.3 生成年份和时长的归一化值
                int year = parseYear(releaseDate);
                double normalizedYear = normalizeValue(year, minYear, maxYear);

                int runtime = parseRuntime(runtimeStr);
                double normalizedRuntime = normalizeValue(runtime, minRuntime, maxRuntime);

                // 3.4 合并特征向量（类型向量 + 年份 + 时长）
                double[] featureVector = mergeVectors(typeVector, normalizedYear, normalizedRuntime);

                // 3.5 转换为字符串（保留两位小数）
                String matrix = Arrays.stream(featureVector)
                        .mapToObj(d -> String.format("%.2f", d))
                        .collect(Collectors.joining(","));

                // 3.6 构建特征记录
                MovieFeature feature = new MovieFeature();
                feature.setMid(mid);
                feature.setMatrix(matrix);
                featureList.add(feature);

            } catch (Exception e) {
                System.err.println("生成特征失败：" + movie.getId() + ", 原因：" + e.getMessage());
            }
        }

        // 4. 批量插入特征表（MyBatis-Plus批量操作）
        featureService.saveBatch(featureList);
        System.out.println("成功生成 " + featureList.size() + " 条电影特征记录");
    }

    // ------------------------- 辅助方法 -------------------------

    /**
     * 提取所有电影的年份
     */
    private List<Integer> extractYears(List<Movie> movies) {
        return movies.stream()
                .map(m -> parseYear(m.getReleaseDate()))
                .collect(Collectors.toList());
    }

    /**
     * 提取所有电影的时长（分钟数）
     */
    private List<Integer> extractRuntimes(List<Movie> movies) {
        return movies.stream()
                .map(m -> parseRuntime(m.getRuntime()))
                .collect(Collectors.toList());
    }

    /**
     * 解析年份（格式：YYYY-MM-DD 或 YYYY）
     */
    private int parseYear(String releaseDate) {
        if (StringUtils.hasText(releaseDate)) {
            String[] parts = releaseDate.split("-");
            return Integer.parseInt(parts[0]);
        }
        return 0;
    }

    /**
     * 解析时长（格式：XXX分钟）
     */
    private int parseRuntime(String runtimeStr) {
        if (StringUtils.hasText(runtimeStr)) {
            return Integer.parseInt(runtimeStr.replace("分钟", "").trim());
        }
        return 0;
    }

    /**
     * 归一化数值到 [0, 1] 区间
     */
    private double normalizeValue(int value, int min, int max) {
        if (min == max) return 0.0;
        return (value - min) / (double) (max - min);
    }

    /**
     * 构建类型独热向量（假设类型ID从1开始，对应索引0-21）
     */
    private double[] buildTypeOneHotVector(String typeStr) {
        double[] vector = new double[TYPE_DIMENSION];
        if (!StringUtils.hasText(typeStr)) return vector;

        String[] typeIds = typeStr.split(",");
        for (String id : typeIds) {
            try {
                int index = Integer.parseInt(id) - 1; // ID=1对应索引0
                if (index >= 0 && index < TYPE_DIMENSION) {
                    vector[index] = 1.0;
                }
            } catch (NumberFormatException e) {
                System.err.println("类型ID解析失败：" + id);
            }
        }
        return vector;
    }

    /**
     * 合并类型向量、年份和时长特征
     */
    private double[] mergeVectors(double[] typeVector, double year, double runtime) {
        double[] result = new double[typeVector.length + 2];
        System.arraycopy(typeVector, 0, result, 0, typeVector.length);
        result[typeVector.length] = year;
        result[typeVector.length + 1] = runtime;
        return result;
    }

}
