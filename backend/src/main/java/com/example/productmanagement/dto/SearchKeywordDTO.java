package com.example.productmanagement.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 搜索关键词统计DTO
 */
@Data
public class SearchKeywordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 搜索次数
     */
    private Long searchCount;

    /**
     * 总结果数
     */
    private Long totalResults;

    /**
     * 排名
     */
    private Integer rank;
}
