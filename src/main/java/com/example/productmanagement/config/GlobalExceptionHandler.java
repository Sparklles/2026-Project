package com.example.productmanagement.config;

import com.example.productmanagement.controller.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 捕获我们在 Service 层主动抛出的 RuntimeException 业务异常
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        log.warn("业务异常拦截: {}", e.getMessage());
        String msg = e.getMessage() != null ? e.getMessage() : "系统内部数据异常(空指针)";
        return Result.error(400, e.getMessage());
    }

    // 兜底捕获不可预知的系统异常
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统未知异常", e);
        return Result.error(500, "系统繁忙，请稍后再试");
    }


}
