package com.example.productmanagement.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 销售汇总DTO
 */
@Data
public class SalesSummaryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总订单数
     */
    private Long totalOrders;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 已付金额
     */
    private BigDecimal paidAmount;

    /**
     * 平均订单金额
     */
    private BigDecimal avgAmount;

    /**
     * 用户总数
     */
    private Long userCount;

    /**
     * 总销售额（别名，前端使用）
     */
    private BigDecimal totalSales;

    /**
     * 总销量（别名，前端使用）
     */
    private Long totalQuantity;

    /**
     * 客单价（别名，前端使用）
     */
    private BigDecimal averageOrderValue;
}
