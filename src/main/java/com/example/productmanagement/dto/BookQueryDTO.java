package com.example.productmanagement.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BookQueryDTO {

    private Long categoryId;
    // 标题（模糊）
    private String title;
    // 作者（模糊）
    private String author;
    // 出版年份区间
    private Integer minYear;
    private Integer maxYear;
    private Integer difficultyTag;
    // 航行地区（模糊）
    private String region;
    // 价格区间
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    // 排序字段，仅允许：sales, favorite_count, avg_rating, price, publish_date
    private String sortField;
    // 排序顺序，asc 或 desc
    private String sortOrder;
}
