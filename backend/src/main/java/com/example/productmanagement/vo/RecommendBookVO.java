package com.example.productmanagement.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RecommendBookVO {
    private Long id;
    private String title;
    private String author;
    private BigDecimal price;
    private String coverImageUrl;
    private Integer difficultyTag;
    private List<String> tags;
}

