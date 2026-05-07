package com.example.productmanagement.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.productmanagement.common.UserStatusReasonConstants;
import com.example.productmanagement.dto.UserStatusAuditLogQueryDto;
import com.example.productmanagement.mapper.UserStatusAuditLogMapper;
import com.example.productmanagement.service.AdminUserStatusAuditService;
import com.example.productmanagement.vo.UserStatusAuditLogVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 后台用户状态审计服务实现
 */
@Service
@RequiredArgsConstructor
public class AdminUserStatusAuditServiceImpl implements AdminUserStatusAuditService {

    private final UserStatusAuditLogMapper userStatusAuditLogMapper;

    @Override
    public IPage<UserStatusAuditLogVo> pageAuditLogs(UserStatusAuditLogQueryDto dto) {
        int pageNum = (dto.getPageNum() == null || dto.getPageNum() < 1) ? 1 : dto.getPageNum();
        int pageSize = (dto.getPageSize() == null || dto.getPageSize() < 1) ? 10 : dto.getPageSize();

        Page<UserStatusAuditLogVo> page = new Page<>(pageNum, pageSize);
        IPage<UserStatusAuditLogVo> result = userStatusAuditLogMapper.selectAuditLogPage(page, dto);
        result.getRecords().forEach(this::fillReasonTypeName);
        return result;
    }

    private void fillReasonTypeName(UserStatusAuditLogVo vo) {
        vo.setReasonTypeName(UserStatusReasonConstants.getReasonName(vo.getAction(), vo.getReasonType()));
    }
}
