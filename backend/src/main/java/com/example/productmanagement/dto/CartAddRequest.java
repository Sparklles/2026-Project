package com.example.productmanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 添加商品到购物车请求参数
 *
 * @author vibe coding
 * @since 2026-04-20
 */
@Data
public class CartAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 书籍ID
     */
    @NotNull(message = "书籍ID不能为空")
    private Long bookId;

    /**
     * 商品数量
     */
    @NotNull(message = "商品数量不能为空")
    @Min(value = 1, message = "商品数量不能小于1")
    private Integer quantity;
}
