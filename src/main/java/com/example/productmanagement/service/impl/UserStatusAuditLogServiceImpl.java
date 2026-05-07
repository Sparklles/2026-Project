package com.example.productmanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.productmanagement.entity.UserStatusAuditLog;
import com.example.productmanagement.mapper.UserStatusAuditLogMapper;
import com.example.productmanagement.service.UserStatusAuditLogService;
import org.springframework.stereotype.Service;

/**
 * 用户状态审计日志服务实现
 */
@Service
public class UserStatusAuditLogServiceImpl extends ServiceImpl<UserStatusAuditLogMapper, UserStatusAuditLog>
        implements UserStatusAuditLogService {
}
