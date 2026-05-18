package com.example.productmanagement.vo;

import lombok.Data;

@Data
public class ThemeRecommendVO {
    private Long id;
    private String title;
    private String author;
    private String description;
    private String coverImageUrl;
}