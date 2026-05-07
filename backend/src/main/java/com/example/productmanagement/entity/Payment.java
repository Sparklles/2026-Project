package com.example.productmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体类
 * 对应数据库表: payment
 *
 * @author vibe coding
 * @since 2026-04-20
 */
@Data
@TableName("payment")
public class Payment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 支付记录ID
     */
    @TableId(value = "pay_id", type = IdType.AUTO)
    private Long payId;

    /**
     * 关联订单
     */
    private Long orderId;

    /**
     * 第三方交易号
     */
    private String tradeNo;

    /**
     * 支付方式: 1微信 2支付宝 3银行卡 4云闪付
     */
    private Integer payType;

    /**
     * 支付金额
     */
    private BigDecimal payAmount;

    /**
     * 支付状态: 0失败 1成功
     */
    private Integer payStatus;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;
}
