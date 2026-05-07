package com.example.productmanagement.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车视图对象
 * 包含购物车信息和关联的书籍信息
 *
 * @author vibe coding
 * @since 2026-04-20
 */
@Data
public class CartVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 购物车ID
     */
    private Long cartId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 书籍ID
     */
    private Long bookId;

    /**
     * 书籍数量
     */
    private Integer quantity;

    /**
     * 书籍名称
     */
    private String bookName;

    /**
     * 书籍作者
     */
    private String author;

    /**
     * 书籍封面
     */
    private String coverUrl;

    /**
     * 书籍单价
     */
    private BigDecimal price;

    /**
     * 商品小计金额
     */
    private BigDecimal subtotal;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 上架状态: 1-已上架, 0-已下架
     */
    private Integer status;

    /**
     * 加入购物车时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
