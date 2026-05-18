package com.example.productmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户账号状态变更审计日志
 */
@Data
@TableName("user_status_audit_log")
public class UserStatusAuditLog {

    /**
     * 日志主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 目标用户 ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 操作管理员 ID
     */
    @TableField("admin_id")
    private Long adminId;

    /**
     * 操作类型：FREEZE / UNFREEZE
     */
    @TableField("action")
    private String action;

    /**
     * 变更前状态
     */
    @TableField("before_status")
    private Integer beforeStatus;

    /**
     * 变更后状态
     */
    @TableField("after_status")
    private Integer afterStatus;

    /**
     * 原因类型
     */
    @TableField("reason_type")
    private String reasonType;

    /**
     * 原因备注
     */
    @TableField("reason_detail")
    private String reasonDetail;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}
