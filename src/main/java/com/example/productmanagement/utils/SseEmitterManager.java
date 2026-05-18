package com.example.productmanagement.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SseEmitterManager {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;

    public String generateKey(Long receiverId, Integer receiverType) {
        return receiverId + "_" + receiverType;
    }

    public SseEmitter createEmitter(Long receiverId, Integer receiverType) {
        String key = generateKey(receiverId, receiverType);

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitter.onCompletion(() -> {
            log.info("SSE连接完成，移除连接: {}", key);
            emitters.remove(key);
        });

        emitter.onTimeout(() -> {
            log.info("SSE连接超时，移除连接: {}", key);
            emitters.remove(key);
        });

        emitter.onError(e -> {
            log.error("SSE连接错误，移除连接: {}, 错误: {}", key, e.getMessage());
            emitters.remove(key);
        });

        SseEmitter oldEmitter = emitters.put(key, emitter);
        if (oldEmitter != null) {
            try {
                oldEmitter.complete();
            } catch (Exception e) {
                log.warn("关闭旧SSE连接失败: {}", e.getMessage());
            }
        }

        log.info("创建SSE连接: {}, 当前连接数: {}", key, emitters.size());
        return emitter;
    }

    public void removeEmitter(Long receiverId, Integer receiverType) {
        String key = generateKey(receiverId, receiverType);
        SseEmitter emitter = emitters.remove(key);
        if (emitter != null) {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.warn("关闭SSE连接失败: {}", e.getMessage());
            }
        }
        log.info("移除SSE连接: {}, 当前连接数: {}", key, emitters.size());
    }

    public boolean sendToUser(Long receiverId, Integer receiverType, Object data) {
        String key = generateKey(receiverId, receiverType);
        SseEmitter emitter = emitters.get(key);

        if (emitter == null) {
            log.debug("用户不在线，无法推送: {}", key);
            return false;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(data));
            log.debug("SSE推送成功: {}", key);
            return true;
        } catch (IOException e) {
            log.error("SSE推送失败: {}, 错误: {}", key, e.getMessage());
            emitters.remove(key);
            return false;
        }
    }

    public boolean isOnline(Long receiverId, Integer receiverType) {
        return emitters.containsKey(generateKey(receiverId, receiverType));
    }

    public int getOnlineCount() {
        return emitters.size();
    }
}
