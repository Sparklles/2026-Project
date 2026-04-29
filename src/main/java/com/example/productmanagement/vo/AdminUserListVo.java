package com.example.productmanagement.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台用户列表视图对象（管理端专用）。
 * <p>聚合 {@code user} + {@code user_detail} 中对管理端有意义的字段，
 * 手机号展示完整值（管理端不脱敏）。</p>
 */
@Data
public class AdminUserListVo {

    // ---- 来自 user 表 ----

    @JsonSerialize(using = ToStringSerializer.class)
    /** 用户 ID */
    private Long userId;

    /** 登录账号 */
    private String loginAccount;

    /** 手机号（管理端完整展示） */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 角色：1-普通用户，2-管理员 */
    private Integer role;

    /** 账号状态：1-正常，0-冻结 */
    private Integer status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 注册时间 */
    private LocalDateTime createTime;

    // ---- 来自 user_detail 表 ----

    /** 昵称 */
    private String nickname;

    /** 头像 URL */
    private String avatarUrl;

    /** 性别：0-保密，1-男，2-女 */
    private Integer gender;
}
