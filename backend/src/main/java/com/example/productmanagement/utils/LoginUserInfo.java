package com.example.productmanagement.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录用户上下文信息，存储于 ThreadLocal 中。
 *
 * <ul>
 *   <li>{@link #userId} — 用户主键 ID（雪花算法）</li>
 *   <li>{@link #role}   — 用户角色：1-普通用户，2-管理员</li>
 * </ul>
 */
@Data
@AllArgsConstructor
public class LoginUserInfo {

    /** 用户主键 ID */
    private Long userId;

    /** 用户角色：1-普通用户，2-管理员 */
    private Integer role;
}
