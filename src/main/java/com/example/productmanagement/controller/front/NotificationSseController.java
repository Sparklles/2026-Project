package com.example.productmanagement.controller.front;

import com.example.productmanagement.common.Result;
import com.example.productmanagement.utils.SseEmitterManager;
import com.example.productmanagement.utils.UserHolder;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
@Validated
public class NotificationSseController {

    private final SseEmitterManager sseEmitterManager;

    public NotificationSseController(SseEmitterManager sseEmitterManager) {
        this.sseEmitterManager = sseEmitterManager;
    }

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(
            @NotNull @RequestParam Integer receiverType) {
        Long receiverId = UserHolder.getUserId();
        return sseEmitterManager.createEmitter(receiverId, receiverType);
    }

    @GetMapping("/close")
    public Result<Void> close(
            @NotNull @RequestParam Integer receiverType) {
        Long receiverId = UserHolder.getUserId();
        sseEmitterManager.removeEmitter(receiverId, receiverType);
        return Result.success("连接已关闭");
    }

    @GetMapping("/online")
    public Result<Boolean> isOnline(
            @NotNull @RequestParam Integer receiverType) {
        Long receiverId = UserHolder.getUserId();
        return Result.success(sseEmitterManager.isOnline(receiverId, receiverType));
    }

    @GetMapping("/count")
    public Result<Integer> getOnlineCount() {
        return Result.success(sseEmitterManager.getOnlineCount());
    }
}
