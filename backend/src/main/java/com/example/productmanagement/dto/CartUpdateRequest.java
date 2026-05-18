package com.example.productmanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新购物车商品数量请求参数
 *
 * @author vibe coding
 * @since 2026-04-20
 */
@Data
public class CartUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 购物车记录ID
     */
    @NotNull(message = "购物车记录ID不能为空")
    private Long cartId;

    /**
     * 商品数量
     */
    @NotNull(message = "商品数量不能为空")
    @Min(value = 1, message = "商品数量不能小于1")
    private Integer quantity;
}
