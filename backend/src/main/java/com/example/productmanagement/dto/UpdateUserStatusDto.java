package com.example.productmanagement.dto;

import lombok.Data;

/**
 * 管理员修改用户账号状态 DTO
 */
@Data
public class UpdateUserStatusDto {

    /**
     * 账号状态：1-正常（解冻），0-冻结/禁用
     */
    private Integer status;

    /**
     * 原因类型
     */
    private String reasonType;

    /**
     * 原因备注
     */
    private String reasonDetail;
}
