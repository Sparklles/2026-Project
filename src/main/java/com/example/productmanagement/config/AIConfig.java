package com.example.productmanagement.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    /**
     * 配置基于内存的对话记忆（开发测试用）
     * 生产环境中建议替换为基于 Redis 的 RedisChatMemory，以支持分布式和持久化
     */
    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }
}
