package com.example.productmanagement.service.impl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
public class AIChatService {

    private final ChatClient chatClient;

    public AIChatService(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
        // 构建一个具有工具调用能力和历史记忆的专属 ChatClient
        this.chatClient = chatClientBuilder
                // 注入专家人设（System Prompt）
                .defaultSystem("""
                    你是一个隶属于“航海时代商城”的专业 AI 导购与客服。
                    你的名字叫“深蓝小助手”。你的语气应该热情、专业、带有航海特色（可以偶尔使用水手、船长等称呼）。
                    
                    你的核心任务：
                    1. 解答用户的航海专业问题。
                    2. 根据用户需求，主动调用工具搜索数据库里的商品并推荐。
                    3. 当用户表达购买意愿（例如：“帮我买一个”、“就下单这个”），提取出商品ID和数量，务必调用下单工具。
                    
                    重要规则：
                    当前正在与你对话的用户的专属 ID 是：{currentUserId}。
                    调用下单工具时，必须使用这个 {currentUserId}。
                    """)
                // 开启对话记忆，保证多轮问答的连贯性
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                // 注册我们刚才写的两个函数工具
                .defaultFunctions("searchProductTool", "createOrderTool")
                .build();
    }

    /**
     * 处理用户对话
     *
     * @param sessionId  会话ID（前端可传一个随机UUID，用于区分不同窗口）
     * @param userId     当前登录用户ID
     * @param userMessage 用户的提问内容
     * @return AI的回答
     */
    public String chatWithAI(String sessionId, Long userId, String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                // 动态绑定系统 Prompt 里的变量（非常关键：让大模型知道当前是哪个用户在说话）
                .system(s -> s.param("currentUserId", String.valueOf(userId)))
                // 绑定当前记忆的会话ID
                .advisors(a -> a.param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId))
                .call()
                .content();
    }
}
