package com.example.productmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.productmanagement.entity.BookInfo;
import com.example.productmanagement.entity.Order;
import com.example.productmanagement.mapper.BookInfoMapper;
import com.example.productmanagement.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIRecommendationService {

    private final VectorStore vectorStore;
    private final BookInfoMapper bookInfoMapper;
    private final OrderMapper orderMapper;

    public List<BookInfo> semanticSearch(String naturalLanguageQuery) {
        SearchRequest searchRequest = SearchRequest.query(naturalLanguageQuery).withTopK(5);
        List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);

        if (similarDocuments.isEmpty()) {
            return Collections.emptyList();
        }

        // 🌟 终极安全提取：直接获取 Document 的原生 ID 作为 bookId！
        List<Long> bookIds = similarDocuments.stream()
                .map(doc -> {
                    try {
                        log.info("✅ 成功匹配文本: {}, 取出 ID: {}", doc.getContent(), doc.getId());
                        return Long.valueOf(doc.getId());
                    } catch (Exception e) {
                        return null; // 如果偶尔碰到旧垃圾数据，安全过滤掉
                    }
                })
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        if (bookIds.isEmpty()) {
            log.warn("⚠️ 警告：检索到文本，但 Document ID 无法转换为 Long。请前往 RedisInsight 执行 FLUSHALL。");
            return Collections.emptyList();
        }

        return bookInfoMapper.selectBatchIds(bookIds);
    }

    public List<BookInfo> recommendForUser(Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId).orderByDesc(Order::getCreateTime).last("LIMIT 3");
        List<Order> recentOrders = orderMapper.selectList(wrapper);

        if (recentOrders.isEmpty()) {
            return semanticSearch("适合新手的初级航海入门书籍和装备");
        }

        String userPreference = recentOrders.stream()
                .map(Order::getRemark)
                .filter(remark -> remark != null && !remark.isEmpty())
                .collect(Collectors.joining("，"));

        String searchQuery = "用户最近买过这些商品：" + userPreference + "。请推荐符合其品味的商品。";
        return semanticSearch(searchQuery);
    }
}