package com.example.productmanagement.exception;


import com.example.productmanagement.result.ResultCodeEnum;
import lombok.Data;

@Data
public class BizIllegalException extends RuntimeException{
    private Integer code;

    public BizIllegalException(Integer code, String message) {
        super(message);
        this.code = code;
    }
    public BizIllegalException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();

    }


}