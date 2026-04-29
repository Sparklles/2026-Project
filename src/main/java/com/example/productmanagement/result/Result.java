package com.example.productmanagement.result;

import lombok.Data;

@Data
public class Result<T> {

    //返回码
    private Integer code;

    //返回消息
    private String message;

    //返回数据
    private T data;

    public Result() {
    }

    private static <T> Result<T> build(T data) {
        Result<T> result = new Result<>();
        if (data != null)
            result.setData(data);
        return result;
    }

    public static <T> Result<T> build(T body, Integer code, String message ) {
        Result<T> result = build(body);
        result.setCode(code);
        result.setMessage(message);
        return result;
    }


    public static <T> Result<T> ok(T data) {
        return build(data, 200,"成功");
    }

    public static <T> Result<T> ok() {
        return Result.ok(null);
    }

    public static <T> Result<T> fail() {
        return build(null,201,"失败");
    }
    public static <T> Result<T> fail(Integer  code,String message) {
        Result<T> result = build(null);
        result.setCode(code);
        result.setMessage( message);
        return result;

    }
}