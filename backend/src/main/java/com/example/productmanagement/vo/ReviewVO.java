package com.example.productmanagement.vo;


import lombok.Data;

@Data
public class ReviewVO {
    private Long id;
    private String username;
    private String avatar;
    private Integer rating;
    private String content;
    private String date;
    // 新增：官方回复内容
    private String adminReply;
}
