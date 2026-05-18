package com.example.productmanagement.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RefundDetailVo {

    // ====================== 1. 退款单基础信息 ======================
    /**
     * 退款单ID
     */
    private Long id;

    /**
     * 退款单号
     */
    private String refundNo;

    /**
     * 退款类型：0-仅退款，1-退货退款
     */
    private Integer refundType;

    /**
     * 退款类型描述
     */
    private String refundTypeDesc;

    /**
     * 退款状态
     */
    private Integer refundStatus;

    /**
     * 退款状态描述
     */
    private String refundStatusDesc;

    // ====================== 2. 退款金额信息 ======================
    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 商品总价（订单中该商品的价格）
     */
    private BigDecimal itemTotalPrice;

    // ====================== 3. 退款原因与说明 ======================
    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 问题描述/退款说明
     */
    private String refundDesc;

    /**
     * 卖家拒绝原因
     */
    private String rejectReason;

    // ====================== 4. 商品信息 ======================
    /**
     * 订单商品ID
     */
    private Long orderItemId;

    /**
     * 商品ID
     */
    private Long bookId;

    /**
     * 商品名称
     */
    private String bookName;

    /**
     * 商品封面图片URL
     */
    private String coverUrl;

    /**
     * 商品单价
     */
    private BigDecimal price;

    /**
     * 购买数量
     */
    private Integer quantity;

    // ====================== 5. 订单信息 ======================
    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 订单成交时间（支付时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime orderPayTime;

    /**
     * 订单实付金额
     */
    private BigDecimal orderPayAmount;

    /**
     * 订单运费
     */
    private BigDecimal freightAmount;

    // ====================== 6. 用户信息 ======================
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    // ====================== 7. 卖家/管理员信息 ======================
    /**
     * 处理管理员ID
     */
    private Long processAdminId;

    /**
     * 处理管理员名称
     */
    private String processAdminName;

    // ====================== 8. 时间信息 ======================
    /**
     * 申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime applyTime;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime auditTime;

    /**
     * 用户寄回时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime returnTime;

    /**
     * 卖家收货时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime receiveTime;

    /**
     * 退款完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime refundFinishTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    // ====================== 9. 协商历史 ======================
    /**
     * 协商历史记录列表
     */
    private List<RefundHistoryVo> historyList;

    /**
     * 当前处理步骤（用于前端展示进度条）
     * 1-买家申请退款，2-卖家处理，3-退款完毕
     */
    private Integer currentStep;
}
