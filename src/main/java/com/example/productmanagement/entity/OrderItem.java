package com.example.productmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("order_item")
public class OrderItem implements Serializable {
    @TableId(value = "item_id", type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long bookId;
    /**
     * 商品名称
     * 🌟 新增注解：告诉 MyBatis-Plus，这个属性对应数据库的 book_name 字段
     */
    @TableField("book_name")
    private String bookTitle;
    /**
     * 商品封面图
     * 🌟 新增注解：对应数据库的 cover_url 字段
     */
    @TableField("cover_url")
    private String coverImageUrl;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;
    private Date createTime;
}
