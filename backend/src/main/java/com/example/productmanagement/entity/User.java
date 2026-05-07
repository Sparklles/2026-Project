package com.example.productmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 系统用户核心表(鉴权与风控)
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User {
    /**
     * 用户主键ID(采用雪花算法生成，非自增)
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 登录账号(系统唯一凭证，非展示昵称)
     */
    @TableField(value = "login_account")
    private String loginAccount;

    /**
     * 登录密码(需使用如BCrypt等哈希加密存储)
     */
    @TableField(value = "password")
    private String password;

    /**
     * 手机号(支持手机验证码登录)
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 联系邮箱/找回密码用
     */
    @TableField(value = "email")
    private String email;

    /**
     * 角色: 1普通用户 2.管理员
     */
    @TableField(value = "role")
    private Integer role;

    /**
     * 账号状态: 1-正常, 0-冻结/禁用(风控干预用)
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 最后登录时间(风控审计)
     */
    @TableField(value = "last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 逻辑删除: 0-未删除, 1-已注销(保护历史数据)
     */
    @TableField(value = "is_deleted")
    private Integer isDeleted;

    /**
     * 注册时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;
}