package com.example.productmanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 删除购物车商品请求参数
 *
 * @author vibe coding
 * @since 2026-04-20
 */
@Data
public class CartDeleteRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 购物车记录ID列表
     */
    @NotNull(message = "购物车记录ID列表不能为空")
    private List<Long> cartIds;
}
