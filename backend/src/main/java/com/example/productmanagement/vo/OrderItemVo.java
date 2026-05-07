package com.example.productmanagement.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemVo {
    private Integer bookId;
    private String bookTitle;
    private String bookAuthor;
    private String coverUrl;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal discount;
}
