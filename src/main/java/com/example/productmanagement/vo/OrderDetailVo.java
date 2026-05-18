package com.example.productmanagement.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailVo {

    // 订单id
    private Long orderId;

    // 订单编号
    private String orderNo;

    // 订单状态
    private Integer orderStatus;

    // 订单支付状态
    private Integer payStatus;

    // 总价格
    private BigDecimal totalPrice;

    // 优惠价格
    private BigDecimal discountPrice;

    // 运费
    private BigDecimal freightPrice;

    // 支付价格
    private BigDecimal payPrice;

    // 支付方式
    private Integer payType;

    // 创建时间
    private LocalDateTime createTime;

    // 支付时间
    private LocalDateTime payTime;

    // 发货时间
    private LocalDateTime shipTime;

    // 完成时间
    private LocalDateTime closeTime;

    // 收货人
    private String consignee;

    // 收货人电话
    private String phone;

    // 收货地址
    private String address;

    // 备注
    private String remark;

    // 商品信息
    private List<OrderItemVo> items;
}
