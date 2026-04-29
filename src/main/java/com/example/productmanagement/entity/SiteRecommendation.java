package com.example.productmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("site_recommendation")
public class SiteRecommendation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String moduleName;
    private Long bookId;
    private Integer sortOrder;
    private Date createTime;
}
