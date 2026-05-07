package com.example.productmanagement.common;

import com.example.productmanagement.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;


import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局异常处理类
 * 统一处理系统中抛出的各类异常
 *
 * @author vibe coding
 * @since 2026-04-20
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler3 {

    /**
     * 处理业务异常
     */
    /*@ExceptionHandler(BusinessException.class)
    public Result<Void> error(BusinessException e) {
        log.warn("业务异常: [{}] {}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    *//**
     * 处理非法参数异常
     *//*
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> error(IllegalArgumentException e) {
        log.warn("参数错误: {}", e.getMessage());
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), e.getMessage());
    }

    *//**
     * 处理空指针异常
     *//*
    @ExceptionHandler(NullPointerException.class)
    public Result<Void> error(NullPointerException e) {
        log.error("空指针异常: ", e);
        return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), "系统错误，请联系管理员");
    }

    *//**
     * 处理非法状态异常
     *//*
    @ExceptionHandler(IllegalStateException.class)
    public Result<Void> error(IllegalStateException e) {
        log.warn("状态异常: {}", e.getMessage());
        return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> error(MethodArgumentNotValidException e) {
        log.warn("【全局异常处理：】 - 参数校验异常: [{}]", e.getMessage());
        BindingResult bindingResult = e.getBindingResult();

        // 获取所有属性错误信息
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        Map<String, String> errorMap = new HashMap<>();

        for (FieldError fieldError : fieldErrors) {
            // 获取错误字段名
            String fieldName = fieldError.getField();
            // 获取错误信息
            String message = fieldError.getDefaultMessage();
            errorMap.put(fieldName, message);
        }

        return Result.error(ErrorCode.PARAM_ERROR.getCode(), ErrorCode.PARAM_ERROR.getMessage(), errorMap);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public Result<?> handleConstraintViolation(HandlerMethodValidationException e) {
        log.warn("【全局异常处理：】 - 单个参数校验异常: [{}]", e.getMessage());

        Map<String,String> errorMap = new HashMap<>();
        for(ParameterValidationResult result:e.getAllValidationResults()){
            String paramName = result.getMethodParameter().getParameterName();
            String message = result.getResolvableErrors().get(0).getDefaultMessage();
            errorMap.put(paramName, message);
        }

        // 直接返回你统一的错误格式
        return Result.error(
                ErrorCode.PARAM_ERROR.getCode(),
                ErrorCode.PARAM_ERROR.getMessage(),
                errorMap
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Result<?> error(NoResourceFoundException e) {
        log.warn("【全局异常处理】- 路径错误：: [{}]", e.getMessage());

        // 直接返回你统一的错误格式
        return Result.error(
                ErrorCode.NOT_FOUND.getCode(),
                ErrorCode.NOT_FOUND.getMessage()
        );
    }

    *//**
     * 处理其他所有异常
     *//*
    @ExceptionHandler(Exception.class)
    public Result<Void> error(Exception e) {
        log.error("系统异常: ", e);
        return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getMessage());
    }*/
}
