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
public class CategoryPreferenceStrategy implements RecommendStrategy {

    private final UserBehaviorLogMapper userBehaviorLogMapper;
    private final BookInfoMapper bookInfoMapper;
    private final BookStatsMapper bookStatsMapper;

    @Override
    public String getType() {
        return "CATEGORY_PREFERENCE";
    }

    @Override
    public List<ScoredBook> execute(Map<String, Object> params) {
        Long userId = params.containsKey("userId") ? ((Number) params.get("userId")).longValue() : null;
        if (userId == null) {
            return new ArrayList<>();
        }

        int limit = StrategyLimits.limit(params.get("limit"), 4);
        int days = StrategyLimits.positiveInt(params.get("days"), 30, 365);

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

        Map<Long, Integer> categoryCountMap = new HashMap<>();
        for (UserBehaviorLog log : logs) {
            BookInfo book = bookInfoMapper.selectById(log.getBookId());
            if (book == null || book.getStatus() != 1 || book.getIsDeleted() != 0 || book.getCategoryId() == null) {
                continue;
            }
            categoryCountMap.merge(book.getCategoryId(), 1, Integer::sum);
        }

        if (categoryCountMap.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> preferredCategories = categoryCountMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
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
            BigDecimal score = scoreMap.getOrDefault(book.getId(), BigDecimal.ZERO);
            result.add(new ScoredBook(book.getId(), score, "你感兴趣的分类"));
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
}
