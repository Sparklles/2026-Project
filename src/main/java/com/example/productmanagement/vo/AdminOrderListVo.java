package com.example.productmanagement.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AdminOrderListVo extends OrderListVo {

    // 1. 买家账户信息
    private Long userId;
    private String username; // 下单用户的昵称/账号

    // 2. 快速联系与发货信息
    private String consignee; // 收货人姓名
    private String phone;     // 收货人电话

}