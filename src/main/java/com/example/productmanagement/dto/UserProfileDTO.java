package com.example.productmanagement.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户画像统计DTO
 */
@Data
public class UserProfileDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类名称（年龄段、性别、会员等级、消费区间等）
     */
    private String categoryName;

    /**
     * 分类值
     */
    private String categoryValue;

    /**
     * 用户数量
     */
    private Long userCount;
}
