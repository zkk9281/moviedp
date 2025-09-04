package com.zkk.moviedp.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zkk.moviedp.entity.Movie;
import com.zkk.moviedp.mapper.MovieMapper;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SyncJob {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    private MovieMapper movieMapper;

    /**
     * 全量同步（首次运行时调用）
     */
    @Transactional(readOnly = true)
    public void fullSync() {
        List<Movie> allMovies = movieMapper.selectList(null);
        for (Movie movie : allMovies) {
            syncMovieToVectorStore(movie);
        }
    }

    /**
     * 增量同步（定时任务，例如每5分钟一次）
     */
    @Scheduled(fixedDelay = 60 * 60 * 1000) // 1h
    @Transactional(readOnly = true)
    public void incrementalSync() {
        // 1. 获取更新时间在一个小时内的电影
        List<Movie> updatedMovies = movieMapper.selectList(
                new QueryWrapper<Movie>()
                        .ge("update_time", Date.from(Instant.now().minus(Duration.ofHours(1))))
        );

        // 2. 遍历处理
        for (Movie movie : updatedMovies) {
            syncMovieToVectorStore(movie);
        }
    }

    /**
     * 核心方法：将一部电影同步到向量库
     */
    private void syncMovieToVectorStore(Movie movie) {
        // 1. 将结构化数据组合成高质量的文本片段
        String textToEmbed = buildMovieText(movie);

        // 2. 为文本生成向量嵌入
        Embedding embedding = embeddingModel.embed(textToEmbed).content();

        // 3. 创建携带元数据的文本段
        TextSegment textSegment = TextSegment.from(
                textToEmbed,
                Metadata.from(new HashMap<>(Map.of(
                        "movie_id", movie.getId(),
                        "title", movie.getName(),
                        "genres", movie.getType(),
                        "year", movie.getReleaseDate()))
                )
        );

        // 4. 先删除该电影旧的嵌入（避免重复），然后存储新的
        // 根据元数据中的movie_id来删除旧记录
        embeddingStore.removeAll(
                new IsEqualTo("movie_id", movie.getId())
        );
        embeddingStore.add(embedding, textSegment);
    }

    /**
     * 最关键的一步：构建用于嵌入的文本
     * 这里决定了AI检索的质量和精度
     */
    private String buildMovieText(Movie movie) {
        // 模板化拼接，尽可能多地包含语义信息
        return String.format(
                "电影名：《%s》。 " +
                        "类型：%s。 " +
                        "导演：%s。 " +
                        "主演：%s。 " +
                        "上映时间：%s。 " +
                        "剧情简介：%s。 " +
                        "地区：%s。",
                movie.getName(),
                movie.getType(),
                movie.getDirectors(),
                movie.getActors(),
                movie.getReleaseDate(),
                movie.getIntroduction(),
                movie.getRegion()
        );
    }
}
