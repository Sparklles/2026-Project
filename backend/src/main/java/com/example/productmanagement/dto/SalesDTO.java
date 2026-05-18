package com.example.productmanagement.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 销售统计DTO
 */
@Data
public class SalesDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 统计日期（按日统计）或统计月份（按月统计）
     */
    private String statDate;

    /**
     * 订单数量
     */
    private Long orderCount;

    /**
     * 总金额
     */
    private BigDecimal totalSales;

    /**
     * 实收金额
     */
    private BigDecimal actualSales;
}
