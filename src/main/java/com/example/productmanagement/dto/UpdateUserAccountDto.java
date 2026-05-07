package com.example.productmanagement.dto;

import lombok.Data;

/**
 * 管理员修改用户账号核心字段 DTO（user 表可编辑字段）
 * 仅管理端可用。
 */
@Data
public class UpdateUserAccountDto {

    /** 登录账号 */
    private String loginAccount;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /**
     * 账号状态字段已迁移至专用状态变更接口，不建议在此接口中传值。
     */
    private Integer status;
}
