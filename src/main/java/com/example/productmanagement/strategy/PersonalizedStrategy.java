package com.example.productmanagement.strategy;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.productmanagement.entity.BookInfo;
import com.example.productmanagement.entity.BookStats;
import com.example.productmanagement.mapper.BookInfoMapper;
import com.example.productmanagement.mapper.BookStatsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PersonalizedStrategy implements RecommendStrategy {

    private final BookStatsMapper bookStatsMapper;
    private final BookInfoMapper bookInfoMapper;

    @Override
    public String getType() {
        return "PERSONALIZED";
    }

    @Override
    public List<ScoredBook> execute(Map<String, Object> params) {
        int limit = StrategyLimits.limit(params.get("limit"), 6);

        LambdaQueryWrapper<BookStats> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(BookStats::getCompositeScore);
        wrapper.last("LIMIT " + (limit * 3));

        List<BookStats> statsList = bookStatsMapper.selectList(wrapper);
        List<Long> bookIds = statsList.stream().map(BookStats::getBookId).collect(Collectors.toList());

        if (bookIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<BookInfo> books = bookInfoMapper.selectBatchIds(bookIds);
        List<ScoredBook> result = new ArrayList<>();
        for (BookStats stats : statsList) {
            boolean valid = books.stream().anyMatch(b ->
                    b.getId().equals(stats.getBookId()) && b.getStatus() == 1 && b.getIsDeleted() == 0);
            if (valid) {
                result.add(new ScoredBook(stats.getBookId(),
                        stats.getCompositeScore() != null ? stats.getCompositeScore() : BigDecimal.ZERO,
                        "猜你喜欢"));
            }
            if (result.size() >= limit) {
                break;
            }
        }
        return result;
    }
}
