package com.example.productmanagement.dto;

import lombok.Data;

/**
 * 验证码重置密码 DTO
 */
@Data
public class ResetPasswordByCodeDto {

    /**
     * 登录账号或手机号
     */
    private String account;

    /**
     * 账号类型：1-手机号，2-登录账号
     */
    private Integer type;

    /**
     * 邮箱验证码
     */
    private String code;

    /**
     * RSA 加密后的新密码
     */
    private String newPassword;
}
