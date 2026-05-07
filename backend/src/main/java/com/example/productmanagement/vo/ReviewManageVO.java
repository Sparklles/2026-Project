package com.example.productmanagement.vo;

import lombok.Data;

@Data
public class ReviewManageVO {
    private Long id;
    private String tabType;     // 'received' 或 'given'
    private String ratingType;  // 'good', 'neutral', 'bad'
    private Boolean isSystem;   // 是否为系统默认评价
    private String content;
    private String date;        // 格式化的时间
    private String targetName;  // 店铺名或买家名
    private String adminReply;  // 商家回复

    // 嵌套的商品信息
    private ProductDetailVO product;
}
