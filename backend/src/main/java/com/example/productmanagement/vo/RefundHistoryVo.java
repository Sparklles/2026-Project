package com.example.productmanagement.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 退款协商历史记录VO
 */
@Data
public class RefundHistoryVo {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 操作人角色：1-买家，2-卖家/系统
     */
    private Integer operatorRole;

    /**
     * 操作人角色描述
     */
    private String operatorRoleDesc;

    /**
     * 操作人名称
     */
    private String operatorName;

    /**
     * 操作人头像（系统消息使用默认头像）
     */
    private String operatorAvatar;

    /**
     * 操作类型：申请退款、审核通过、审核拒绝、退款完成等
     */
    private String actionType;

    /**
     * 操作内容/说明
     */
    private String content;

    /**
     * 退款金额（如果有）
     */
    private String refundAmount;

    /**
     * 退款原因（申请时）
     */
    private String refundReason;

    /**
     * 拒绝原因（拒绝时）
     */
    private String rejectReason;

    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime operateTime;
}
