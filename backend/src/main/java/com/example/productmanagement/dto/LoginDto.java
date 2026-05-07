package com.example.productmanagement.dto;

import lombok.Data;

@Data
public class LoginDto {

    private String account;

    private String password;
    /**
     * 登录类型
     * 1、用户手机号和密码登录，此时account为手机号
     * 2、用户账户和密码登录，此时account为用户账户
     */
    private Integer type;

    private Integer expectedRole; // 登录期望的角色 1为普通用户，2为管理员
}