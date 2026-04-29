package com.example.productmanagement.dto;

import lombok.Data;

/**
 * 注册请求 DTO
 *
 * <ul>
 *   <li>type=1：手机号 + 密码注册，此时 account 填手机号</li>
 *   <li>type=2：账号（字母+数字）+ 密码注册，此时 account 填自定义账号</li>
 * </ul>
 * password 字段由前端用 RSA 公钥加密后传输。
 */
@Data
public class RegisterDto {

    /**
     * 注册账号：
     * type=1 时为手机号（纯数字，11位）；
     * type=2 时为自定义登录账号（字母+数字组合，4-20位）
     */
    private String account;

    /**
     * 登录密码（前端用 RSA 公钥加密后的 Base64 密文）
     */
    private String password;

    /**
     * 注册类型：1-手机号注册，2-账号注册
     */
    private Integer type;
}
