package com.example.productmanagement.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UpdateOrderStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String orderNo;

    private Integer orderStatus;
}
