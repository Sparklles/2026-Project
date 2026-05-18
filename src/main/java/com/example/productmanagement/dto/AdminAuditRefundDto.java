package com.example.productmanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 *  卖家审核退款请求dto
 */
@Data
public class AdminAuditRefundDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "管理员Id不能为空")
    private Long adminId;

    @NotNull(message = "退款单ID不能为空")
    private Long refundId;

    @NotNull(message = "审核结果不能为空")
    private Boolean approved;


    private String rejectReason;
}
