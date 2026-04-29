package com.example.productmanagement.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class BookDTO {
    /** 只有在执行"修改"操作时才会传ID，新增时不传 */
    private Long id;
    private Long categoryId;
    private String title;
    private String author;
    private String isbn;
    private BigDecimal price;
    private Integer pages;
    private Date publishDate;
    private String description;
    private String coverImageUrl;
    private Integer stock;

    /** 重点：前端传过来的选中标签的ID集合，用于处理多对多关联 */
    private List<Long> tagIds;
    /** 新增：前端传过来的图片 URL 列表，第一张默认为封面 */
    private List<String> imageUrls;
}
