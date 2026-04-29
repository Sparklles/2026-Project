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

    // 🌟 就是因为缺了它，导致了刚才的崩溃！
    private String isbn;

    private BigDecimal price; // 如果您原来用的是 Double，改成 Double 也可以

    private String description;

    private String coverImageUrl;

    private Integer stock;

    private Integer status;

    private Long categoryId;

    private String categoryName;

    // 用于接收我们 Java 代码里塞进去的标签列表
    private List<String> tags;
    /** 新增：返回给前端的回显图片列表 */
    private List<String> images;

    // 🌟 新增：为了前端编辑时能够回显，必须加上这两个字段
    private Integer pages;

    // 🌟 保证传给前端的时间格式是 yyyy-MM-dd
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date publishDate;
}
