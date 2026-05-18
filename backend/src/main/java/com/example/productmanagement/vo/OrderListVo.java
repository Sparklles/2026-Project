package com.example.productmanagement.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderListVo implements Serializable {

    private static final long serialVersionUID = 1L;

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

    // 创建时间
    private LocalDateTime createTime;

    // 商品信息
    private List<OrderItemVo> items;

}
