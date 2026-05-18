package com.example.productmanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.productmanagement.dto.UserStatusAuditLogQueryDto;
import com.example.productmanagement.vo.UserStatusAuditLogVo;

/**
 * 后台用户状态审计服务
 */
public interface AdminUserStatusAuditService {

    /**
     * 分页查询用户状态审计日志
     */
    IPage<UserStatusAuditLogVo> pageAuditLogs(UserStatusAuditLogQueryDto dto);
}
