package com.example.productmanagement.dto;

import lombok.Data;

import java.io.Serializable;

/**
 *  订单操作dto
 */
@Data
public class OrderOperateDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;

    private String orderNo;
}
