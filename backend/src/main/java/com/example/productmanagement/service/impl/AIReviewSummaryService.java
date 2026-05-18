package com.example.productmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.productmanagement.dto.ReviewSummaryResult;
import com.example.productmanagement.entity.BookReview;
import com.example.productmanagement.entity.BookStats;
import com.example.productmanagement.mapper.BookReviewMapper;
import com.example.productmanagement.mapper.BookStatsMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AIReviewSummaryService {

    private final ChatClient chatClient;
    private final BookReviewMapper reviewMapper;
    private final BookStatsMapper statsMapper;
    private final ObjectMapper objectMapper;

    public AIReviewSummaryService(ChatClient.Builder chatClientBuilder,
                                  BookReviewMapper reviewMapper,
                                  BookStatsMapper statsMapper,
                                  ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                    你是一位资深的航海装备测评专家。
                    请仔细阅读用户提供的多条真实商品评价，提取出大家最关心的优点和缺点，并给出客观的综合评分（0-100分）。
                    请严格遵守输出格式，不要输出任何额外的废话、问候语或 Markdown 标记。
                    """)
                .build();
        this.reviewMapper = reviewMapper;
        this.statsMapper = statsMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 🌟 核心修复：将 void 改为 String。
     * 逻辑：先查库看有没有现成的，有就秒回；没有就当场呼叫大模型生成并返回！
     */
    public String getOrGenerateReviewSummary(Long bookId) {
        // 1. 先查数据库，看之前是否已经生成过（作为缓存，省钱省时间！）
        BookStats stats = statsMapper.selectById(bookId);
        if (stats != null && stats.getAiSummary() != null && !stats.getAiSummary().trim().isEmpty()) {
            log.info("商品 ID: {} 命中 AI 评价缓存，直接返回！", bookId);
            return stats.getAiSummary(); // 👈 数据库有，直接返回 JSON 给前端
        }

        log.info("未命中缓存，开始为商品 ID: {} 现场生成 AI 评价总结...", bookId);

        // 2. 数据库没有，去查真实评价
        LambdaQueryWrapper<BookReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookReview::getBookId, bookId)
                .eq(BookReview::getStatus, 1)
                .orderByDesc(BookReview::getCreateTime)
                .last("LIMIT 50");
        List<BookReview> reviews = reviewMapper.selectList(wrapper);

        if (reviews.isEmpty()) {
            log.warn("商品 ID: {} 暂无评价，跳过生成。", bookId);
            return "暂无 AI 总结数据"; // 👈 一条评论都没有，返回提示
        }

        // 3. 拼接给大模型看的提示词
        String reviewTextBatch = reviews.stream()
                .map(r -> "用户打分 " + r.getRating() + "星，评价内容：" + r.getContent())
                .collect(Collectors.joining("\n---\n"));

        try {
            // 4. 呼叫大模型并转为 Java Record
            ReviewSummaryResult aiResult = chatClient.prompt()
                    .user(u -> u.text("以下是该航海装备的真实用户评价：\n{reviews}")
                            .param("reviews", reviewTextBatch))
                    .call()
                    .entity(ReviewSummaryResult.class);

            // 5. 转为 JSON 字符串
            String jsonSummary = objectMapper.writeValueAsString(aiResult);

            // 6. 落库保存，下次就不用再调大模型了
            if (stats == null) {
                stats = new BookStats();
                stats.setBookId(bookId);
                stats.setAiSummary(jsonSummary);
                stats.setAiSentimentScore(aiResult.sentimentScore());
                statsMapper.insert(stats);
            } else {
                stats.setAiSummary(jsonSummary);
                stats.setAiSentimentScore(aiResult.sentimentScore());
                statsMapper.updateById(stats);
            }

            log.info("商品 ID: {} AI 总结现场生成成功！", bookId);

            // 🌟 最关键的一步：将刚生成的热乎数据返回！
            return jsonSummary;

        } catch (Exception e) {
            log.error("商品 ID: {} AI 总结生成失败", bookId, e);
            return "暂无 AI 总结数据";
        }
    }
}