package com.example.productmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户详细信息表
 * @TableName user_detail
 */
@TableName(value ="user_detail")
@Data
public class UserDetail {
    /**
     * 用户ID(主键，直接对应user表的雪花ID)
     */
    @TableId(value = "user_id")
    private Long userId;

    /**
     * 用户昵称(用于前台评论、社区展示等)
     */
    @TableField(value = "nickname")
    private String nickname;

    /**
     * 用户头像存放路径/URL
     */
    @TableField(value = "avatar_url")
    private String avatarUrl;

    /**
     * 性别: 0-保密/未知, 1-男, 2-女
     */
    @TableField(value = "gender")
    private Integer gender;

    /**
     * 出生日期(可用于推送生日相关的营销活动)
     */
    @TableField(value = "birthday")
    private LocalDate birthday;

    /**
     * 个性签名/个人简介
     */
    @TableField(value = "signature")
    private String signature;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;
}