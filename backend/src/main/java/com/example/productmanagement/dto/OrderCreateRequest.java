package com.example.productmanagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    @NotNull(message = "地址ID不能为空")
    private Long addressId;

    @NotEmpty(message = "订单商品不能为空")
    private List<OrderItemDTO>  orderItems;


    // ========可选=========
    private String remark;
}
