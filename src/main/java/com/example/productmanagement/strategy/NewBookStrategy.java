package com.example.productmanagement.strategy;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.productmanagement.entity.BookInfo;
import com.example.productmanagement.mapper.BookInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NewBookStrategy implements RecommendStrategy {

    private final BookInfoMapper bookInfoMapper;

    @Override
    public String getType() {
        return "NEW";
    }

    @Override
    public List<ScoredBook> execute(Map<String, Object> params) {
        int limit = StrategyLimits.limit(params.get("limit"), 6);

        LambdaQueryWrapper<BookInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookInfo::getStatus, 1)
                .eq(BookInfo::getIsDeleted, 0)
                .isNotNull(BookInfo::getPublishDate)
                .orderByDesc(BookInfo::getPublishDate);
        wrapper.last("LIMIT " + limit);

        List<BookInfo> books = bookInfoMapper.selectList(wrapper);
        List<ScoredBook> result = new ArrayList<>();
        for (int i = 0; i < books.size(); i++) {
            BookInfo book = books.get(i);
            result.add(new ScoredBook(book.getId(),
                    BigDecimal.valueOf(books.size() - i), "新书上架"));
        }
        return result;
    }
}
