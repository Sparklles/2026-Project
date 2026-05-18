package com.example.productmanagement.dto;

import lombok.Data;

@Data
public class ReviewQueryDTO {
    private String tabType;       // received 或 given
    private String filterType;    // all, good, neutral, bad
    private String filterContent; // all, hasContent, noContent
    private String keyword;       // 搜索关键字
}
