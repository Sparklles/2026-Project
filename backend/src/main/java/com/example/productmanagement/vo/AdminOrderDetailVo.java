package com.example.productmanagement.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *  管理员视角订单详细
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminOrderDetailVo extends OrderDetailVo {

    // ================= 1. 买家用户信息 =================
    /**
     * 下单用户ID (用于快速查询该用户过往订单或拉黑操作)
     */
    private Long userId;

    /**
     * 下单用户名
     */
    private String username;
}
