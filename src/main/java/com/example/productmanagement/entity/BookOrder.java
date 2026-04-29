package com.example.productmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("book_order")
public class BookOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    /** 订单状态: 0-待付款, 1-已付款待发货, 2-已发货, 3-已完成, 4-已取消 */
    private Integer status;
    private String shippingAddress;
    private String trackingNumber;
    private Date createTime;
    private Date updateTime;
}
