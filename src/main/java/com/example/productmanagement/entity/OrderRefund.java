package com.example.productmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_refund")
public class OrderRefund implements Serializable {
    private static final long serialVersionUID = 1L;


    // 主键
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 退款编号
    private String refundNo;

    // 关联订单商品id
    private Long orderItemId;

    // 关联用户
    private Long userId;

    // 处理管理员
    private Long processAdmin;

    // 退款类型（1退货退款/0仅退款）
    private Integer refundType;

    // 退款原因
    private String refundReason;

    // 问题描述
    private String refundDesc;

    // 退款金额
    private BigDecimal refundAmount;

    // 退款状态
    private Integer refundStatus;

    // 卖家拒绝原因
    private String rejectReason;

    // 用户申请时间
    private LocalDateTime applyTime;

    // 卖家审核时间
    private LocalDateTime auditTime;

    // 用户寄回时间
    private LocalDateTime returnTime;

    // 卖家收货时间
    private LocalDateTime receiveTime;

    // 退款结束时间
    private LocalDateTime refundFinishTime;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;
}
