package com.example.productmanagement.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.example.productmanagement.dto.UserSearchDto;
import com.example.productmanagement.mapper.UserMapper;
import com.example.productmanagement.service.AdminSearchUserService;
import com.example.productmanagement.vo.AdminUserListVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 后台用户管理 Service 实现
 */
@Service
@RequiredArgsConstructor
public class AdminSearchUserServiceImpl implements AdminSearchUserService {

    private final UserMapper userMapper;

    @Override
    public IPage<AdminUserListVo> pageUsers(UserSearchDto dto) {
        // 防御性校验：分页参数不合法时使用默认值
        int pageNum  = (dto.getPageNum()  == null || dto.getPageNum()  < 1) ? 1  : dto.getPageNum();
        int pageSize = (dto.getPageSize() == null || dto.getPageSize() < 1) ? 10 : dto.getPageSize();

        Page<AdminUserListVo> page = new Page<>(pageNum, pageSize);
        return userMapper.selectAdminUserPage(page, dto);
    }
}
