package com.example.productmanagement.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 *  订单支付请求，暂时没有优惠功能，可留在迭代2
 */
@Data
public class PayOrderRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String orderNo;

    private Integer payType;
}
