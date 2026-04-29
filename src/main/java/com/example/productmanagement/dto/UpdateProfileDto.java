package com.example.productmanagement.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 用户修改个人详情 DTO（user_detail 表可编辑字段）
 * 适用于用户端和管理端修改 user_detail 记录。
 */
@Data
public class UpdateProfileDto {

    /** 用户昵称 */
    private String nickname;

    /** 头像 URL */
    private String avatarUrl;

    /**
     * 性别：0-保密，1-男，2-女
     */
    private Integer gender;

    /** 出生日期 */
    private LocalDate birthday;

    /** 个性签名 */
    private String signature;
}
