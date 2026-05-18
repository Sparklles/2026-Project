package com.example.productmanagement.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 专门用于“我的收藏”页面展示的数据对象
 */
@Data
public class FavoriteVO {
    /** 这里返回的是商品ID，方便前端点击跳转详情 */
    private Long id;
    private String title;
    private String image;
    private BigDecimal price;

    // 新增：价格波动标识和差额
    private BigDecimal priceDiff; // 价格差额的绝对值
    private Integer priceStatus;  // 1: 降价了, -1: 涨价了, 0: 平价无变化

    /** 收藏人数 (字符串格式，如 "1.2万+") */
    private String favCount;

    /** 分类标识 (前端Tab用的：books / devices) */
    private String category;

    /** 是否自营 */
    private Boolean isSelfOperated;

    /** 商品状态：1正常，0失效下架 */
    private Integer status;

    /** 前端用的选中状态，后端默认给 false 即可 */
    private Boolean selected;
}
