package com.example.productmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单物流信息实体类
 * 对应数据库表: order_logistics
 *
 * @author vibe coding
 * @since 2026-04-20
 */
@Data
@TableName("order_logistics")
public class OrderLogistics implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 物流主键ID
     */
    @TableId(value = "logistics_id", type = IdType.AUTO)
    private Long logisticsId;

    /**
     * 关联订单编号（一对一）
     */
    private Long orderId;

    /**
     * 发货地址/仓库地址
     */
    private String shipAddress;

    /**
     * 发货省
     */
    private String shipProvince;

    /**
     * 发货市
     */
    private String shipCity;

    /**
     * 收货详细地址
     */
    private String receiveAddress;

    /**
     * 收货省
     */
    private String receiveProvince;

    /**
     * 收货市
     */
    private String receiveCity;

    /**
     * 收货人姓名
     */
    private String receiveName;

    /**
     * 收货人电话
     */
    private String receivePhone;

    /**
     * 物流单号
     */
    private String logisticsNo;

    /**
     * 物流公司名称
     */
    private String carrierName;

    /**
     * 物流状态：0-待发货 1-已揽收 2-运输中 3-派送中 4-已签收 5-异常
     */
    private Integer logisticsStatus;

    /**
     * 当前所在位置（如：上海市浦东新区转运中心）
     */
    private String currentLocation;

    /**
     * 发货时间
     */
    private LocalDateTime shipTime;

    /**
     * 签收时间
     */
    private LocalDateTime receiveTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
