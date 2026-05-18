package com.example.productmanagement.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.RedisVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorStoreConfig {

    @Value("${spring.ai.vectorstore.redis.index:book_info_index}")
    private String index;

    @Value("${spring.ai.vectorstore.redis.prefix:book_doc:}")
    private String prefix;

    // 🌟 读取你在 application.properties 里配置的云服务器 Redis URI
    @Value("${spring.ai.vectorstore.redis.uri}")
    private String redisUri;

    @Bean
    public VectorStore vectorStore(
            // 🌟 强行指定使用智谱 AI 的模型，解决一山不容二虎的冲突！
            @Qualifier("zhiPuAiEmbeddingModel") EmbeddingModel embeddingModel) {

        // 🌟 核心修复 1：将 URI 配置进 Config 里
        RedisVectorStore.RedisVectorStoreConfig config = RedisVectorStore.RedisVectorStoreConfig.builder()
                .withURI(redisUri)
                .withIndexName(index)
                .withPrefix(prefix)
                .build();

        // 🌟 核心修复 2：删掉 jedisPooled，只传它需要的 3 个参数！
        return new RedisVectorStore(config, embeddingModel, true);
    }
}