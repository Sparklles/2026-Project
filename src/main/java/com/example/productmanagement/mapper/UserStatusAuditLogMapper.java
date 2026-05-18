package com.example.productmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.productmanagement.dto.UserStatusAuditLogQueryDto;
import com.example.productmanagement.entity.UserStatusAuditLog;
import com.example.productmanagement.vo.UserStatusAuditLogVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户状态审计日志 Mapper
 */
@Mapper
public interface UserStatusAuditLogMapper extends BaseMapper<UserStatusAuditLog> {

    /**
     * 审计日志分页查询
     */
    IPage<UserStatusAuditLogVo> selectAuditLogPage(@Param("page") IPage<UserStatusAuditLogVo> page,
                                                   @Param("params") UserStatusAuditLogQueryDto params);
}
