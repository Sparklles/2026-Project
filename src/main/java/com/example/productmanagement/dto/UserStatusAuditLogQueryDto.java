package com.example.productmanagement.dto;

import lombok.Data;

/**
 * 用户状态审计日志查询参数
 */
@Data
public class UserStatusAuditLogQueryDto {

    /**
     * 目标用户登录账号（模糊）
     */
    private String targetLoginAccount;

    /**
     * 管理员登录账号（模糊）
     */
    private String adminLoginAccount;

    /**
     * 操作类型：FREEZE / UNFREEZE
     */
    private String action;

    /**
     * 原因类型编码
     */
    private String reasonType;

    /**
     * 当前页
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;
}
