package com.example.productmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName shipping_address
 */
@TableName(value ="shipping_address")
@Data
public class ShippingAddress {
    /**
     * 主键ID(雪花算法)
     */
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 收货人姓名
     */
    @TableField(value = "consignee_name")
    private String consigneeName;

    /**
     * 收货人手机号
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 省份/直辖市
     */
    @TableField(value = "province")
    private String province;

    /**
     * 城市
     */
    @TableField(value = "city")
    private String city;

    /**
     * 区/县
     */
    @TableField(value = "district")
    private String district;

    /**
     * 详细地址(街道、门牌号等)
     */
    @TableField(value = "detail_address")
    private String detailAddress;

    /**
     * 是否为默认地址: 0-否, 1-是
     */
    @TableField(value = "is_default")
    private Integer isDefault;

    /**
     * 
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;
}