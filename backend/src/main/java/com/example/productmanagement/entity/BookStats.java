package com.example.productmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("book_stats")
public class BookStats {

    @TableId(type = IdType.INPUT)   // 与 book_info.id 一致，非自增
    private Long bookId;

    private Integer sales;
    private BigDecimal avgRating;
    private Integer reviewCount;
    private Integer favoriteCount;
    private BigDecimal compositeScore;

    // ==========================================
    // 🌟 新增：AI 智能评价总结相关字段
    // ==========================================

    /**
     * AI 基于用户评价生成的智能总结
     * (对应数据库字段 ai_summary，存储为 JSON 字符串)
     */
    private String aiSummary;

    /**
     * AI 分析出来的情感倾向分数 (0-100分)
     * (对应数据库字段 ai_sentiment_score)
     */
    private Integer aiSentimentScore;

    @Version
    private Integer version;

    private Date updateTime;
}
