package com.example.productmanagement.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 书籍详情与列表展示视图对象 (VO)
 * 聚合了分类名称和标签集合，专供前端展示使用
 */
@Data // 这个 Lombok 注解会自动生成所有字段的 getter 和 setter
public class BookDetailVO {

    private Long id;

    private String title;

    private String author;

    private String isbn;

    private BigDecimal price;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date publishDate;

    private String description;

    private String coverImageUrl;

    private Integer stock;

    private Integer sales;

    private Integer status;

    private Long categoryId;

    private Integer difficultyTag;

    private String region;

    private String categoryName;

    private BigDecimal avgRating;

    private Integer reviewCount;

    private Integer favoriteCount;

    private BigDecimal compositeScore;
    // 用于接收我们 Java 代码里塞进去的标签列表
    private List<String> tags;

    private List<String> images;

    private Integer pages;
}
