package com.example.productmanagement.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 管理员退款单列表VO
 * 在RefundListVo基础上扩展管理员需要的用户信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminRefundListVo extends RefundListVo {

    /**
     * 申请用户ID
     */
    private Long userId;

    /**
     * 申请用户名
     */
    private String username;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 处理管理员ID
     */
    private Long processAdminId;

    /**
     * 处理管理员名称
     */
    private String processAdminName;

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
     * 退款完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime refundFinishTime;

    /**
     * 退款状态描述
     */
    private String refundStatusDesc;

    /**
     * 退款类型描述
     */
    private String refundTypeDesc;
}
