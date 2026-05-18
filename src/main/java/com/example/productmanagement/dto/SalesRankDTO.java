package com.example.productmanagement.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 销量排行DTO
 */
@Data
public class SalesRankDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 书籍ID
     */
    private Long bookId;

    /**
     * 书籍名称
     */
    private String bookName;

    /**
     * ISBN
     */
    private String isbn;

    /**
     * 作者
     */
    private String author;

    /**
     * 出版社
     */
    private String publisher;

    /**
     * 分类
     */
    private String category;

    /**
     * 销售数量
     */
    private Integer totalQuantity;

    /**
     * 销售金额
     */
    private BigDecimal totalSales;

    /**
     * 排名
     */
    private Integer rank;
}
