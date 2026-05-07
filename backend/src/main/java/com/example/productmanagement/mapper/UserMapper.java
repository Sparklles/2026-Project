package com.example.productmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.example.productmanagement.dto.UserSearchDto;
import com.example.productmanagement.entity.User;
import com.example.productmanagement.vo.AdminUserListVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author freedom
 * @description 针对表【user(系统用户核心表(鉴权与风控))】的数据库操作Mapper
 * @createDate 2026-04-21 15:14:41
 * @Entity com.dujiang.sailmart.entity.User
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 后台用户列表分页查询（LEFT JOIN user_detail，支持三维度模糊搜索）。
     *
     * @param page   MyBatis-Plus 分页对象
     * @param params 搜索条件（loginAccount / email / phone，任意组合）
     * @return 分页结果
     */
    IPage<AdminUserListVo> selectAdminUserPage(
            @Param("page") IPage<AdminUserListVo> page,
            @Param("params") UserSearchDto params);
}




