package com.example.productmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("recommendation_item")
public class RecommendationItem {
    @TableId(type = IdType.INPUT)
    private Long id;
    private String sceneCode;
    private Long userId;
    private Long sourceBookId;
    private Long targetBookId;
    private BigDecimal score;
    private String recommendReason;
    private Integer sortOrder;
    private Date expireTime;
    private Date createTime;
    private Date updateTime;
}