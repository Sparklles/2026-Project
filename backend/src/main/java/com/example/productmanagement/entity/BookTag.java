package com.example.productmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("book_tag")
public class BookTag {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    //新增：排序权值
    private Integer sortOrder;
    private Date createTime;
}
