package com.example.productmanagement.config;

import com.example.productmanagement.service.impl.ProductVectorSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VectorStartupRunner implements ApplicationRunner {

    private final VectorStore vectorStore;
    private final ProductVectorSyncService syncService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("--- 🤖 正在检查 AI 向量数据库状态 ---");
        try {
            // 随便搜索一个行业关键词，测试 Redis 库里有没有数据
            var results = vectorStore.similaritySearch(SearchRequest.query("航海").withTopK(1));

            if (results == null || results.isEmpty()) {
                log.info("💡 检测到 Redis 向量库为空，正在自动为您构建 AI 知识库...");
                // 自动触发同步，你再也不用手动去浏览器访问了！
                syncService.syncAllToVectorStore();
            } else {
                // 如果已经有数据了，就跳过。这样重启服务器时就不会浪费 API Token 扣费了
                log.info("✅ AI 向量知识库已就绪，跳过初始化。");
            }
        } catch (Exception e) {
            log.error("❌ 向量库检查失败，请确保 Redis 运行正常且智谱 API-Key 正确。", e);
        }
    }
}
