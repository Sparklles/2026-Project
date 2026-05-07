package com.example.productmanagement.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;

    private String orderNo;

    private Long userId;

    private String username;

    private Integer orderStatus;

    private Integer payStatus;

    private Integer payType;

    private LocalDateTime payTime;

    private BigDecimal totalAmount;

    private BigDecimal payAmount;

    private BigDecimal discountAmount;

    private String consignee;

    private String phone;

    private String address;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<OrderItemVo> orderItemVos;
}
