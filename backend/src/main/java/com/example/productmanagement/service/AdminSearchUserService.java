package com.example.productmanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.productmanagement.dto.UserSearchDto;
import com.example.productmanagement.vo.AdminUserListVo;


/**
 * 后台用户管理 Service
 */
public interface AdminSearchUserService {

    /**
     * 分页查询用户列表，支持按 loginAccount / email / phone 模糊搜索。
     *
     * @param dto 搜索参数（含分页信息）
     * @return 分页结果
     */
    IPage<AdminUserListVo> pageUsers(UserSearchDto dto);
}
