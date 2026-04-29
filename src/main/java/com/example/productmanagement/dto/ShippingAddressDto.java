package com.example.productmanagement.dto;

import lombok.Data;

/**
 * 收货地址 DTO（新增 / 修改地址时前端传入）
 */
@Data
public class ShippingAddressDto {

    /** 收货人姓名（必填） */
    private String consigneeName;

    /** 收货人手机号（必填） */
    private String phone;

    /** 省份/直辖市（必填） */
    private String province;

    /** 城市（必填） */
    private String city;

    /** 区/县（必填） */
    private String district;

    /** 详细地址，街道、门牌号等（必填） */
    private String detailAddress;

    /** 是否设为默认地址：0-否，1-是（可选，默认 0） */
    private Integer isDefault;
}
