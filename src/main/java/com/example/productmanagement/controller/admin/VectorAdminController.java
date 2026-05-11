package com.example.productmanagement.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.productmanagement.entity.BookInfo;
import com.example.productmanagement.mapper.BookInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/admin/vector")
@RequiredArgsConstructor
public class VectorAdminController {

    private final BookInfoMapper bookInfoMapper;

    // 注入我们配置好的 Redis 向量库（底层会自动调用智谱 AI 的 Embedding 接口）
    private final VectorStore vectorStore;

    /**
     * 核心动作：将 MySQL 的商品数据同步到 Redis 向量库
     * (为了方便你测试，这里故意写成了 @GetMapping，可以直接在浏览器里触发)
     */
    @GetMapping("/sync-all")
    public String syncAllBooksToVectorStore() {
        log.info("--- 开始执行商品数据向量化同步 ---");

        // 1. 从 MySQL 中查出所有未下架的商品
        LambdaQueryWrapper<BookInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookInfo::getIsDeleted, 0).eq(BookInfo::getStatus, 1);
        List<BookInfo> books = bookInfoMapper.selectList(wrapper);

        if (books.isEmpty()) {
            return "MySQL 中没有商品，无法同步！";
        }

        // 2. 将商品转化为 Spring AI 认识的 Document 对象
        List<Document> documents = books.stream().map(book -> {
            // 🌟 这一步非常关键！把你想让 AI 理解的信息拼接成一段自然语言文本
            String content = String.format("这是一件航海商品，商品名称是【%s】。作者/品牌：%s。商品描述：%s。",
                    book.getTitle(),
                    book.getAuthor(),
                    book.getDescription() != null ? book.getDescription() : "暂无详情");

            // 将商品 ID 等原数据存入 Metadata，搜索出来后方便根据 ID 去查数据库
            return new Document(content, Map.of(
                    "bookId", book.getId(),
                    "title", book.getTitle(),
                    "price", book.getPrice()
            ));
        }).collect(Collectors.toList());

        // 3. 批量写入 Redis（这步耗时较长，因为需要调用智谱 API 将文字转为向量）
        log.info("准备将 {} 条商品数据发送给智谱 AI 转化为向量，并写入 Redis...", documents.size());
        vectorStore.add(documents);

        log.info("--- 向量化同步完美结束！ ---");
        return "成功同步 " + documents.size() + " 条商品到向量数据库！现在去聊天框搜索吧！";
    }
}