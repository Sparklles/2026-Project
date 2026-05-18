package com.example.productmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.productmanagement.entity.BookInfo;
import com.example.productmanagement.mapper.BookInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductVectorSyncService {

    private final BookInfoMapper bookInfoMapper;
    private final VectorStore vectorStore;

    /**
     * 统一全量同步：将 MySQL 所有上架商品打碎成向量存入 Redis
     */
    public void syncAllToVectorStore() {
        log.info("🚀 开始全量同步商品数据到 AI 向量库...");
        LambdaQueryWrapper<BookInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookInfo::getIsDeleted, 0).eq(BookInfo::getStatus, 1);
        List<BookInfo> books = bookInfoMapper.selectList(wrapper);

        if (books.isEmpty()) {
            log.warn("⚠️ MySQL 暂无上架商品，跳过 AI 同步。");
            return;
        }

        List<Document> documents = books.stream().map(book -> {
            String content = String.format("航海商品，名称：【%s】。作者/品牌：%s。详情：%s。",
                    book.getTitle(), book.getAuthor() != null ? book.getAuthor() : "未知",
                    book.getDescription() != null ? book.getDescription() : "暂无");

            // 🌟 终极必杀技：直接把真实的 bookId 当作 Document 的原生全局 ID！不再依赖脆弱的 Metadata！
            return new Document(
                    String.valueOf(book.getId()),
                    content,
                    Map.of("title", book.getTitle() != null ? book.getTitle() : "未知")
            );
        }).collect(Collectors.toList());

        vectorStore.add(documents);
        log.info("✅ 成功同步 {} 条商品至 AI 向量库！", documents.size());
    }

    /**
     * 单条增量同步
     */
    public void syncSingleProduct(BookInfo book) {
        if (book.getStatus() != null && book.getStatus() == 0) return;

        String content = String.format("航海商品，名称：【%s】。作者/品牌：%s。详情：%s。",
                book.getTitle(), book.getAuthor() != null ? book.getAuthor() : "未知",
                book.getDescription() != null ? book.getDescription() : "暂无");

        // 🌟 终极必杀技：同样把 ID 设为原生 ID
        Document doc = new Document(
                String.valueOf(book.getId()),
                content,
                Map.of("title", book.getTitle() != null ? book.getTitle() : "未知")
        );
        vectorStore.add(List.of(doc));
        log.info("✅ 商品【{}】已自动加入 AI 知识库！", book.getTitle());
    }

    public void syncAllProductsToVectorStore() {
        syncAllToVectorStore();
    }
}