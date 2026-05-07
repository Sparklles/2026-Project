package com.example.productmanagement.controller.profile;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.productmanagement.dto.UserStatusAuditLogQueryDto;
import com.example.productmanagement.result.Result;
import com.example.productmanagement.service.AdminUserStatusAuditService;
import com.example.productmanagement.vo.UserStatusAuditLogVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端用户状态审计日志 Controller
 */
@RestController
@RequestMapping("/api/admin/user-status-audit")
@RequiredArgsConstructor
public class AdminUserStatusAuditController {

    private final AdminUserStatusAuditService adminUserStatusAuditService;

    /**
     * 分页查询用户状态变更审计日志。
     */
    @GetMapping("/page")
    public Result<IPage<UserStatusAuditLogVo>> pageAuditLogs(UserStatusAuditLogQueryDto dto) {
        return Result.ok(adminUserStatusAuditService.pageAuditLogs(dto));
    }
}
