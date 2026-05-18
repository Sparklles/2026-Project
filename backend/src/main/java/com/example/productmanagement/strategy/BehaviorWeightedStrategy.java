package com.example.productmanagement.strategy;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.productmanagement.entity.BookInfo;
import com.example.productmanagement.entity.BookStats;
import com.example.productmanagement.entity.UserBehaviorLog;
import com.example.productmanagement.mapper.BookInfoMapper;
import com.example.productmanagement.mapper.BookStatsMapper;
import com.example.productmanagement.mapper.UserBehaviorLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BehaviorWeightedStrategy implements RecommendStrategy {

    private final UserBehaviorLogMapper userBehaviorLogMapper;
    private final BookInfoMapper bookInfoMapper;
    private final BookStatsMapper bookStatsMapper;

    private static final Map<String, Integer> BEHAVIOR_TYPE_MAP = Map.of(
            "purchase", 6,
            "favorite", 5,
            "addCart", 4,
            "click", 3,
            "search", 2,
            "browse", 1
    );

    @Override
    public String getType() {
        return "BEHAVIOR_WEIGHTED";
    }

    @Override
    public List<ScoredBook> execute(Map<String, Object> params) {
        Long userId = params.containsKey("userId") ? ((Number) params.get("userId")).longValue() : null;
        if (userId == null) {
            return new ArrayList<>();
        }

        int limit = StrategyLimits.limit(params.get("limit"), 4);
        int days = StrategyLimits.positiveInt(params.get("days"), 14, 365);

        Map<Integer, Double> behaviorWeights = buildBehaviorWeights(params);

        Date cutoff = Date.from(LocalDateTime.now().minusDays(days)
                .atZone(ZoneId.systemDefault()).toInstant());

        LambdaQueryWrapper<UserBehaviorLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(UserBehaviorLog::getUserId, userId)
                .ge(UserBehaviorLog::getCreateTime, cutoff)
                .isNotNull(UserBehaviorLog::getBookId);
        List<UserBehaviorLog> logs = userBehaviorLogMapper.selectList(logWrapper);

        if (logs.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> purchasedBookIds = logs.stream()
                .filter(l -> Objects.equals(l.getBehaviorType(), 6))
                .map(UserBehaviorLog::getBookId)
                .collect(Collectors.toSet());

        Map<Long, Double> categoryScoreMap = new HashMap<>();
        Map<Long, BookInfo> bookInfoMap = new HashMap<>();

        for (UserBehaviorLog log : logs) {
            Double weight = behaviorWeights.getOrDefault(log.getBehaviorType(), 0.0);
            if (weight <= 0) {
                continue;
            }

            BookInfo book = bookInfoMap.computeIfAbsent(log.getBookId(),
                    id -> bookInfoMapper.selectById(id));
            if (book == null || book.getStatus() != 1 || book.getIsDeleted() != 0 || book.getCategoryId() == null) {
                continue;
            }

            categoryScoreMap.merge(book.getCategoryId(), weight, Double::sum);
        }

        if (categoryScoreMap.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> preferredCategories = categoryScoreMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        LambdaQueryWrapper<BookInfo> bookWrapper = new LambdaQueryWrapper<>();
        bookWrapper.in(BookInfo::getCategoryId, preferredCategories)
                .eq(BookInfo::getStatus, 1)
                .eq(BookInfo::getIsDeleted, 0);
        if (!purchasedBookIds.isEmpty()) {
            bookWrapper.notIn(BookInfo::getId, purchasedBookIds);
        }
        List<BookInfo> candidates = bookInfoMapper.selectList(bookWrapper);

        if (candidates.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> candidateIds = candidates.stream().map(BookInfo::getId).collect(Collectors.toList());
        List<BookStats> statsList = bookStatsMapper.selectBatchIds(candidateIds);
        Map<Long, BigDecimal> scoreMap = new HashMap<>();
        for (BookStats stats : statsList) {
            scoreMap.put(stats.getBookId(),
                    stats.getCompositeScore() != null ? stats.getCompositeScore() : BigDecimal.ZERO);
        }

        List<ScoredBook> result = new ArrayList<>();
        for (BookInfo book : candidates) {
            BigDecimal baseScore = scoreMap.getOrDefault(book.getId(), BigDecimal.ZERO);
            Double categoryBoost = categoryScoreMap.getOrDefault(book.getCategoryId(), 0.0);
            BigDecimal finalScore = baseScore.add(BigDecimal.valueOf(categoryBoost));
            result.add(new ScoredBook(book.getId(), finalScore, "根据你的浏览偏好"));
            if (result.size() >= limit * 3) {
                break;
            }
        }

        result.sort((a, b) -> b.getScore().compareTo(a.getScore()));
        if (result.size() > limit) {
            result = result.subList(0, limit);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, Double> buildBehaviorWeights(Map<String, Object> params) {
        Map<Integer, Double> weights = new HashMap<>();
        weights.put(6, 5.0);
        weights.put(5, 3.0);
        weights.put(4, 2.0);
        weights.put(3, 1.0);
        weights.put(1, 0.5);
        weights.put(2, 0.3);

        Object bwParam = params.get("behaviorWeights");
        if (bwParam instanceof Map) {
            Map<String, Object> configWeights = (Map<String, Object>) bwParam;
            for (Map.Entry<String, Object> entry : configWeights.entrySet()) {
                Integer type = BEHAVIOR_TYPE_MAP.get(entry.getKey());
                if (type != null) {
                    weights.put(type, ((Number) entry.getValue()).doubleValue());
                }
            }
        }
        return weights;
    }
}
