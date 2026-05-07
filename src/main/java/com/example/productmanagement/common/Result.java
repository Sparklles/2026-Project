package com.example.productmanagement.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装类
 *
 * @author vibe coding
 * @since 2026-04-20
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码: 200成功, 其他为失败
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(ErrorCode.SUCCESS.getCode(),
                ErrorCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ErrorCode.SUCCESS.getCode(),
                ErrorCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> success(String message) {
        return new Result<>(ErrorCode.SUCCESS.getCode(),
                message, null);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), message, data);
    }



    /**
     * 失败响应
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ErrorCode.SYSTEM_ERROR.getCode(), message, null);
    }

    /**
     * 失败响应（自定义状态码）
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     *  失败响应（自定义）
     */
    public static <T> Result<T> error(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }

    /**
     * 参数错误
     */
    public static <T> Result<T> badRequest(String message) {
        return new Result<>(400, message, null);
    }

    /**
     * 未授权
     */
    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(401, message, null);
    }

    /**
     * 禁止访问
     */
    public static <T> Result<T> forbidden(String message) {
        return new Result<>(403, message, null);
    }

    /**
     * 资源不存在
     */
    public static <T> Result<T> notFound(String message) {
        return new Result<>(404, message, null);
    }
}
