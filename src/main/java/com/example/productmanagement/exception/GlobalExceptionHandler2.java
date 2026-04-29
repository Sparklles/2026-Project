package com.example.productmanagement.exception;


import com.example.productmanagement.result.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler2 {

    @ExceptionHandler(Exception.class)
    public Result handle(Exception e) {
        e.printStackTrace();
        return Result.fail();
    }

    @ExceptionHandler(BizIllegalException.class)
    public Result handle(BizIllegalException e) {
        e.printStackTrace();
        return Result.fail(e.getCode(), e.getMessage());
    }
}

