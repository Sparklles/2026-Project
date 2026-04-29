package com.example.productmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("book_info")
public class BookInfo {
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
    private Integer sales;
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
