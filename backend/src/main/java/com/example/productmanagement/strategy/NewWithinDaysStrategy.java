package com.example.productmanagement.strategy;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.productmanagement.entity.BookInfo;
import com.example.productmanagement.entity.BookStats;
import com.example.productmanagement.mapper.BookInfoMapper;
import com.example.productmanagement.mapper.BookStatsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NewWithinDaysStrategy implements RecommendStrategy {

    private final BookInfoMapper bookInfoMapper;
    private final BookStatsMapper bookStatsMapper;

    @Override
    public String getType() {
        return "NEW_WITHIN_DAYS";
    }

    @Override
    public List<ScoredBook> execute(Map<String, Object> params) {
        int days = StrategyLimits.positiveInt(params.get("days"), 7, 365);
        int limit = StrategyLimits.limit(params.get("limit"), 2);

        Date cutoff = Date.from(LocalDate.now().minusDays(days).atStartOfDay(ZoneId.systemDefault()).toInstant());

        LambdaQueryWrapper<BookInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookInfo::getStatus, 1)
                .eq(BookInfo::getIsDeleted, 0)
                .ge(BookInfo::getPublishDate, cutoff)
                .orderByDesc(BookInfo::getPublishDate);
        wrapper.last("LIMIT " + limit);

        List<BookInfo> books = bookInfoMapper.selectList(wrapper);
        List<ScoredBook> result = new ArrayList<>();
        for (int i = 0; i < books.size(); i++) {
            result.add(new ScoredBook(books.get(i).getId(),
                    BigDecimal.valueOf(books.size() - i), "近期新书"));
        }
        return result;
    }
}
