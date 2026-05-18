package com.example.productmanagement.controller.front;


import com.example.productmanagement.result.Result;
import com.example.productmanagement.service.impl.AIChatService;
import com.example.productmanagement.utils.UserHolder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIChatController {

    private final AIChatService aiChatService;

    @Data
    public static class ChatRequest {
        private String sessionId; // 前端生成的会话ID，用于保持记忆
        private String message;   // 用户说的话
    }

    @PostMapping("/chat")
    public Result<String> chat(@RequestBody ChatRequest request) {
        // 从拦截器中安全获取当前登录的 userId
        Long userId = UserHolder.getLoginUser().getUserId();

        if (userId == null) {
            return Result.error(401, "请先登录后再使用智能导购功能");
        }

        // 调用 AI 服务进行对话、搜索或自动下单
        String aiResponse = aiChatService.chatWithAI(request.getSessionId(), userId, request.getMessage());

        return Result.ok(aiResponse);
    }
}
