package com.example.productmanagement.dto;

import lombok.Data;

@Data
public class ReviewDTO {
    /** 被评价的书籍ID */
    private Long bookId;

    /** 评分 (1-5星) */
    private Integer rating;

    /** 评价文字内容 */
    private String content;
}