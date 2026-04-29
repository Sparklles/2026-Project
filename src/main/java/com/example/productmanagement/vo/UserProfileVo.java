package com.example.productmanagement.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户个人信息视图对象（脱敏后对外返回）。
 *
 * <p>聚合 {@code user} 表和 {@code user_detail} 表中适合对外展示的字段，
 * 不包含密码、is_deleted 等敏感/内部字段。
 */
@Data
public class UserProfileVo {

    // ---- 来自 user 表 ----

    @JsonSerialize(using = ToStringSerializer.class)
    /** 用户 ID */
    private Long userId;

    /** 登录账号 */
    private String loginAccount;


    /** 手机号（脱敏：仅管理端返回完整值，用户端可做掩码） */
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

    /** 出生日期 */
    private LocalDate birthday;

    /** 个性签名 */
    private String signature;
}
