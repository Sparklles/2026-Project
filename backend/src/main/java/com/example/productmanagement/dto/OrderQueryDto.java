package com.example.productmanagement.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class OrderQueryDto implements Serializable {
    private static final long serialVersionUID = 1L;

    // 用户id
    @NotNull(message = "用户id不能为空")
    private Long userId;

    // 1. 时间区间
    @PastOrPresent(message = "日期不合法")
    private LocalDateTime beginTime;

    private LocalDateTime endTime;

    // 2. 订单状态
    @PositiveOrZero(message = "状态不合法")
    private Integer orderStatus;

    // 3. 商品类别ID
    @PositiveOrZero(message = "商品分类无效")
    private Long categoryId;

    // 分页参数
    private Integer current = 1;
    private Integer size = 10;
}
