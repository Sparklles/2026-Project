package com.example.productmanagement.controller.profile;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.example.productmanagement.dto.UserSearchDto;
import com.example.productmanagement.result.Result;
import com.example.productmanagement.service.AdminSearchUserService;
import com.example.productmanagement.vo.AdminUserListVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台用户管理 Controller
 * <p>
 * 路由前缀 {@code /admin/users}，所有接口需管理员权限（由拦截器保障）。
 * </p>
 */
@RestController
@RequestMapping("/api/admin/search/users")
@RequiredArgsConstructor
public class AdminSearchUserController {

    private final AdminSearchUserService adminUserService;

    /**
     * 用户列表分页查询。
     *
     * <p>支持三个维度的模糊搜索（可任意组合，均为可选参数）：
     * <ul>
     *   <li>{@code loginAccount} — 按登录账号模糊匹配</li>
     *   <li>{@code email}        — 按邮箱模糊匹配</li>
     *   <li>{@code phone}        — 按手机号模糊匹配</li>
     * </ul>
     * 不传搜索条件时返回全量分页数据（按注册时间倒序）。
     *
     * @param dto 搜索 + 分页参数
     * @return 分页用户列表（含 nickname、avatarUrl 等基本信息）
     */
    @GetMapping("/page")
    public Result<IPage<AdminUserListVo>> pageUsers(UserSearchDto dto) {
        return Result.ok(adminUserService.pageUsers(dto));
    }
}
