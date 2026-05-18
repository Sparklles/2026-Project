package com.example.productmanagement.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RefundListVo {
    /**
     * 主键
     */
    private Long id;
    /**
     * 退款单号
     */
    private String refundNo;
    /**
     * 订单号
     */
    private String orderNo;
    /**
     * 封面图片
     */
    private String coverUrl;
    /**
     * 书名
     */
    private String bookName;
    /**
     * 退款状态
     */
    private Integer refundStatus;
    /**
     * 退款类型
     */
    private Integer refundType;
    /**
     * 退款金额
     */
    private String refundAmount;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
