package com.example.productmanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class AdminProcessRefundDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "管理员id不能为空")
    private Long adminId;

    @NotNull(message = "退款单ID不能为空")
    private Long refundId;
}
