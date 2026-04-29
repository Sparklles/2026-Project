package com.example.productmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("book_info")
public class BookInfo {
    /**
     * 年龄分段/适用水平：0-ALL, 1-BEGINNER, 2-INTERMEDIATE, 3-ADVANCED
     */
    public static final Integer ALL = 0;
    public static final Integer BEGINNER = 1;
    public static final Integer INTERMEDIATE = 2;
    public static final Integer ADVANCED = 3;

    @TableId(type = IdType.AUTO)
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

    //年龄分段/适用水平：0-ALL, 1-BEGINNER, 2-INTERMEDIATE, 3-ADVANCED
    private Integer difficultyTag;
    //航行地区(如: 太平洋, 加勒比海, 地中海)
    private String region;

    /** 上架状态: 1-已上架, 0-已下架 */
    private Integer status;

    /** 逻辑删除: 0-未删除, 1-已删除。MyBatis-Plus会自动拦截带有此注解的删除操作，转为UPDATE语句 */
    @TableLogic
    private Integer isDeleted;

    /** 乐观锁版本号: 防并发修改 */
    @Version
    private Integer version;

    private Date createTime;
    private Date updateTime;
}
