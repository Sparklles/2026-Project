package com.example.productmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("book_stats")
public class BookStats {

    @TableId(type = IdType.INPUT)   // 与 book_info.id 一致，非自增
    private Long bookId;

    private Integer sales;
    private BigDecimal avgRating;
    private Integer reviewCount;
    private Integer favoriteCount;
    private BigDecimal compositeScore;

    @Version
    private Integer version;

    private Date updateTime;
}
