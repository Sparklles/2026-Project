package com.example.productmanagement.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApplyRefundDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long orderItemId;

    private Integer refundType;

    private String refundReason;

    private String description;


}
