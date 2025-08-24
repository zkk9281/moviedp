package com.zkk.moviedp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkk.moviedp.dto.RecommendedMovie;
import com.zkk.moviedp.dto.Result;
import com.zkk.moviedp.entity.*;
import com.zkk.moviedp.mapper.BlogMapper;
import com.zkk.moviedp.mapper.MovieMapper;
import com.zkk.moviedp.mapper.RecommendationMapper;
import com.zkk.moviedp.service.IMovieFeatureService;
import com.zkk.moviedp.service.IMovieService;
import com.zkk.moviedp.service.IRecommendationService;
import com.zkk.moviedp.service.ITypeLikeService;
import com.zkk.moviedp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class RecommendationServiceImpl extends ServiceImpl<RecommendationMapper, Recommendation>
        implements IRecommendationService {

    @Autowired
    private BlogMapper blogMapper;

    /*private static Recommender userCfRecommender;
    // 基于用户的协同过滤推荐，重新计算获取推荐器
     /*
//    @Async("asyncServiceExecutor")
//    @Scheduled(fixedRate = 1000 * 60 * 20)
    public void updateUserBasedCollaborativeFilteringRecommendationRecommender() throws TasteException {
        log.info("开始：基于用户的协同过滤推荐，重新计算获取推荐器");

        try {
            // 准备数据
            FastByIDMap<PreferenceArray> preferences = new FastByIDMap<>();
            List<Preference> myPreferences = blogMapper.selectAllPreferences();
            log.info("Preference: {}", myPreferences);
            long idx = 0;
            for (int i = 0; i < myPreferences.size(); i ++ ) {
                int cnt = myPreferences.get(i).getCnt();
                PreferenceArray preferenceArray = new GenericUserPreferenceArray(cnt);
                preferenceArray.setUserID(0, myPreferences.get(i).getUserId());
                for (int j = 0; j < cnt; j ++ ) {
                    preferenceArray.setItemID(j, myPreferences.get(i + j).getMovieId());
                    preferenceArray.setValue(j, myPreferences.get(i + j).getScore());
                }
                preferences.put(idx ++, preferenceArray);
                i += cnt - 1;
            }
            DataModel model = new GenericDataModel(preferences);

            // 计算用户间的皮尔逊系数
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            // 对每个用户取固定数量30个最近邻居
            UserNeighborhood neighborhood = new NearestNUserNeighborhood(10, similarity, model);
            // 基于用户协同过滤推荐的推荐器
            userCfRecommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
        } catch (TasteException e) {
            e.printStackTrace();
        }

        log.info("结束：基于用户的协同过滤推荐，重新计算获取推荐器");
    }*/

    /**
     * 推荐电影数量
     */
    private static final int THIRTY_RECOMMENDATIONS = 10;

    @Autowired
    private RecommendationMapper recommendationMapper;

    @Autowired
    private IMovieService movieService;

    @Autowired
    private IMovieFeatureService movieFeatureService;

    @Autowired
    private ITypeLikeService typeLikeService;

    @Override
    public List<RecommendedMovie> getRecommendedMoviesByUserId() {
        Long uid = UserHolder.getUser().getId();
        List<Recommendation> list = query().eq("uid", uid).orderByDesc("idx").list();
        List<RecommendedMovie> res = new ArrayList<>();
        for(Recommendation r : list) {
            Movie movie = movieService.getById(r.getMid());
            RecommendedMovie recommendedMovie = BeanUtil.copyProperties(movie, RecommendedMovie.class);
            recommendedMovie.setIdx(r.getIdx());
            res.add(recommendedMovie);
        }
        return res;
    }

    /**
     * 更新电影推荐
     * @param uid 用户id
     */
    @Override
    public void updateRecommendation(Long uid) {
        log.info("start executeAsync");
        Timestamp t1 = Timestamp.valueOf(LocalDateTime.now());

        // 删除该用户之前的推荐结果
        recommendationMapper.delete(new LambdaQueryWrapper<Recommendation>().eq(Recommendation::getUid, uid));

        // 基于内容推荐
        List<Pair<Long, Double>> contentBasedResult = getContentBasedMovieRecommendationResult(uid,  10);
        log.info("内容推荐产生的推荐电影：{}", contentBasedResult);
        for (Pair<Long, Double> longDoublePair : contentBasedResult) {
            // 重新插入系统新推荐的电影
            recommendationMapper.insert(new Recommendation(uid,
                    longDoublePair.getKey(),
                    longDoublePair.getValue(),
                    1));
        }

        // 协同过滤推荐
        /*try {
            updateUserBasedCollaborativeFilteringRecommendationRecommender();
            List<RecommendedItem> recommendedMovies = userCfRecommender.recommend(uid, 10);
            log.info("协同过滤产生的推荐电影：{}", recommendedMovies);
            for (int i = 0; i < recommendedMovies.size(); i ++ ) {
                // 判断电影是否已经看过，如果没有则插入推荐表中（去重）
                if (blogMapper.selectOne(
                        new LambdaQueryWrapper<Blog>()
                                .eq(Blog::getMovieId, recommendedMovies.get(i).getItemID())
                                .eq(Blog::getUserId, uid)) == null) {
                    recommendationMapper.insert(new Recommendation(uid,
                            recommendedMovies.get(i).getItemID(),
                            recommendedMovies.get(i).getValue() / 10.0,
                            2));
                    cnt ++;
                }
                if (cnt >= 10) {
                    break;
                }
            }
        } catch (TasteException e) {
            e.printStackTrace();
        }*/

        // 数量不足则推荐评分高的电影
        List<Recommendation> recommendations =
                recommendationMapper.selectList(new LambdaQueryWrapper<Recommendation>().eq(Recommendation::getUid, uid));
        int size = recommendations.size();
        if (size < THIRTY_RECOMMENDATIONS) {
            List<Recommendation> recommendations1 = randomRecommended(uid, 10 - size);
            saveBatch(recommendations1);
        }
        Timestamp t2 = Timestamp.valueOf(LocalDateTime.now());
        log.info("begin time:{}", t1);
        log.info("end time:{}", t2);
        log.info("end executeAsync");
    }

    /**
     * 评分随机推荐
     */
    public List<Recommendation> randomRecommended(Long uid, Integer num) {
        List<Recommendation> newRecommendations = new ArrayList<>();
        List<Movie> movies = movieService.query()
                .orderByDesc("score") // 按评分降序排列
                .last("LIMIT " + num) // 取前num条记录;
                .list();
        for (Movie movie : movies) {
            Recommendation r = new Recommendation(uid, movie.getId(), 0.6, 3);
            newRecommendations.add(r);
        }
        return newRecommendations;
    }

    /**
     * 基于内容电影推荐
     */
    public List<Pair<Long, Double>> getContentBasedMovieRecommendationResult(long uid, int size) {
        // 计算用户的偏好矩阵
        double[] userPreferenceMatrix = computeUserPreferenceMatrix(uid);
        System.out.println("userPreferenceMatrix:" + Arrays.toString(userPreferenceMatrix));

        List<Pair<Long, Double>> movieRecommendations = new ArrayList<>();
        // 所有电影特征信息矩阵都提前预处理过并存入数据库中
        // 查询所有电影并排序
        List<MovieFeature> movieFeatures = movieFeatureService.query().list();
        for (MovieFeature movieFeature : movieFeatures) {
            double[] movieFeatureMatrix = formatMovieFeatureMatrix(movieFeature.getMatrix());
            // 计算 用户的偏好矩阵 与 电影的特征信息矩阵 的相似度（运用余弦相似度计算公式）
            double dist = calculateTheUsersPreferenceForMovies(userPreferenceMatrix, movieFeatureMatrix);
            // 若相似度大于0，则加入结果集
            if (dist > 0) {
                movieRecommendations.add(new Pair<>(movieFeature.getMid(), dist));
            }
        }
        // 经过排序，将相似度高的电影排在前面
        movieRecommendations.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        // 取结果集中相似度最高的size部电影，如果结果集没有size部，则取结果集中所有电影
        int len = Math.min(movieRecommendations.size(), size);
        List<Pair<Long, Double>> res = new ArrayList<>();
        for (int i = 0; i < len; i ++ ) {
            res.add(movieRecommendations.get(i));
        }
        return res;
    }

    /**
     * 将电影特征矩阵由字符串转为数组
     * @param feature 电影特征矩阵字符串（例："10100000000..."）
     * @return 数组形式电影特征矩阵（例：[1.0, 0.0, 1.0, 0.0, ...]
     */
    private double[] formatMovieFeatureMatrix(String feature) {
        double[] movieFeatureMatrix = new double[24];
        String[] features = feature.split(",");
        for (int i = 0; i < 24; i ++ ) {
            movieFeatureMatrix[i] = Double.parseDouble(features[i]);
        }
        return movieFeatureMatrix;
    }

    /**
     * 计算用户对电影的喜好程度
     * 即计算 用户的偏好矩阵 与 电影的特征矩阵 的相似度（运用余弦相似度公式）
     * @param userPreferenceMatrix 用户偏好矩阵
     * @param movieFeatureMatrix 电影特征矩阵
     * @return 用户对电影的喜好程度
     */
    private double calculateTheUsersPreferenceForMovies(double[] userPreferenceMatrix, double[] movieFeatureMatrix) {
        INDArray user = Nd4j.create(userPreferenceMatrix, new int[]{1, userPreferenceMatrix.length});
        INDArray movie = Nd4j.create(movieFeatureMatrix, new int[]{1, movieFeatureMatrix.length});
        INDArray mulRes = user.mul(movie);
        // sum()计算矩阵所有元素和，toDoubleVector()将INDArray转为double[]并取第一个值，即矩阵中元素和
        double numerator = mulRes.sum().toDoubleVector()[0];

        double userPowSum = user.mul(user).sum().toDoubleVector()[0];
        double moviePowSum = movie.mul(movie).sum().toDoubleVector()[0];
        double denominator = Math.sqrt(userPowSum) * Math.sqrt(moviePowSum);

        return numerator / denominator;
    }

    /**
     * 计算用户的偏好矩阵
     * @param uid 用户id
     * @return 用户偏好矩阵
     */
    private double[] computeUserPreferenceMatrix(Long uid) {
        List<Blog> blogs = blogMapper.selectList(new LambdaQueryWrapper<Blog>()
                .eq(Blog::getUserId, uid)
                .orderByDesc(Blog::getCreateTime));
        // 如果用户评价过的电影超过5部，则只取最近评价的5部电影
        int blogsLen = Math.min(blogs.size(), 5);

        // typeMap用于存储键值对（电影类型id => 评分）
        Map<Integer, List<Integer>> typeMap = new HashMap<>(22);
        for(int i = 0; i < 22; i++) {
            typeMap.put(i, new ArrayList<>());
        }
        // 从用户的点评行为中获取用户的喜好
        for (int i = 0; i < blogsLen; i++) {
            Integer score = blogs.get(i).getScore();
            Long movieId = blogs.get(i).getMovieId();
            Movie movie = movieService.getById(movieId);
            String types = movie.getType();
            String[] typeList = types.split(",");
            for (String s : typeList) {
                int tid = Integer.parseInt(s);
                typeMap.get(tid).add(score);
            }
        }
        // 用户自己选择的类型喜好标签，每个标签都以10分影响程度加入用户偏好矩阵
        addTypeLikesIntoUserMatrix(uid, typeMap);

        List<Double> res = new ArrayList<>();
        // 计算用户对每个类型的喜好程度，并将最终结果加入到用户偏好矩阵
        makeUserMatrix(typeMap, res);

        // 将res（ArrayList）转为数组
        double[] userPreferenceMatrix = new double[24];
        for (int i = 0; i < res.size(); i ++ ) {
            userPreferenceMatrix[i] = res.get(i);
        }
        userPreferenceMatrix[22] = 0.7;
        userPreferenceMatrix[23] = 0.7;
        return userPreferenceMatrix;
    }

    /**
     * 用户自己选择的类型喜好标签，每个标签都以10分影响程度加入用户偏好矩阵。
     * 冷启动
     * 为了提高用户自选标签的推荐价值，这里加入特征矩阵的次数为USER_LIKE_WEIGHT次
     * @param uid 用户id
     * @param typeMap typeMap用于存储键值对（电影类型id => 评分）
     */
    private void addTypeLikesIntoUserMatrix(Long uid, Map<Integer, List<Integer>> typeMap) {
        List<TypeLike> typeLikes = typeLikeService.query().eq("uid", uid).list();
        for (TypeLike typeLike: typeLikes) {
            if (typeMap.containsKey(typeLike.getTid())) {
                for (int i = 0; i < 3; i ++ ) {
                    typeMap.get(typeLike.getTid()).add(10);
                }
            } else {
                List<Integer> scores = new ArrayList<>();
                for (int i = 0; i < 3; i ++ ) {
                    scores.add(10);
                }
                typeMap.put(typeLike.getTid(), scores);
            }
        }
    }

    /**
     * 计算用户对每个类型和每个地区的喜好程度，并将最终结果加入到用户偏好矩阵
     * @param map 类型 键值对（类型，评分列表）
     * @param res 用户的偏好矩阵
     */
    private void makeUserMatrix(Map<Integer, List<Integer>> map, List<Double> res) {
        for (int i = 0; i < 22; i ++ ) {
            double totalScore = 0.0;
            if (map.containsKey(i)) {
                List<Integer> scores = map.get(i);
                if(scores.isEmpty()) {res.add(0.0); continue;}
                for (Integer score : scores) {
                    totalScore += score;
                }
                res.add(totalScore / scores.size() / 10.0);
            } else {
                res.add(0.0);
            }
        }
    }
}
