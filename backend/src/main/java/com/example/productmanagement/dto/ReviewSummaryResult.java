package com.example.productmanagement.dto;

import java.util.List;

/**
 * AI 评价智能总结的结构化返回载体
 */
public record ReviewSummaryResult(
        List<String> pros,             // 提炼出的核心优点（最多3条）
        List<String> cons,             // 提炼出的核心缺点（最多2条）
        String comprehensiveSummary,   // 给新用户的综合购买建议（50字以内）
        Integer sentimentScore         // 综合情感得分（0-100分，低于60说明口碑极差）
) {}
