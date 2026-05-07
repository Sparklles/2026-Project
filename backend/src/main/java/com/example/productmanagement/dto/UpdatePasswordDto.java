package com.example.productmanagement.dto;

import lombok.Data;

/**
 * 修改密码请求 DTO
 * 
 * <p>oldPassword 和 newPassword 均为前端使用 RSA 公钥加密后的 Base64 密文。
 */
@Data
public class UpdatePasswordDto {

    /**
     * 旧密码（RSA 加密后的 Base64 密文）
     */
    private String oldPassword;

    /**
     * 新密码（RSA 加密后的 Base64 密文）
     */
    private String newPassword;
}
