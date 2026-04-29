package com.example.productmanagement.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDetailVO {
    private Long id;
    private String title;
    private String subtitle; // 对应数据库的 description
    private BigDecimal minPrice; // 对应数据库的 price

    // 原本是多图，现在我们把 coverImageUrl 包装成 List 传给前端，适配前端画廊组件
    private List<String> images;

    // 商品库存
    private Integer stock;

    // 商品标签 (从 book_tag_relation 查出)
    private List<String> tags;

    // 用户评价列表
    private List<ReviewVO> reviews;
}