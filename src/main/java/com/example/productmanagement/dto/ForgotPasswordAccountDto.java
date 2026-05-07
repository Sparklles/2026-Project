package com.example.productmanagement.dto;

import lombok.Data;

/**
 * 忘记密码账号识别 DTO
 */
@Data
public class ForgotPasswordAccountDto {

    /**
     * 登录账号或手机号
     */
    private String account;

    /**
     * 账号类型：1-手机号，2-登录账号
     */
    private Integer type;
}
