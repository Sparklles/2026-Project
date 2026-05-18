package com.example.productmanagement.dto;

import lombok.Data;

@Data
public class ReviewUpdateDTO {
    private Long id;         // 评价的记录ID
    private Integer rating;  // 修改后的星级 (1-5)
    private String content;  // 修改后的内容
}
