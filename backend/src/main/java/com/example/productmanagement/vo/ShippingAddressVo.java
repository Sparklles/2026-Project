package com.example.productmanagement.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 收货地址视图对象（返回给前端）
 */
@Data
public class ShippingAddressVo {

    /** 地址主键 ID */
    // 关键：告诉 Spring MVC，把这个 Long 转成 String 再返回给前端,否则会溢出（JS不支持这么大）
    @JsonSerialize(using = ToStringSerializer.class)
    /** 地址主键 ID */
    private Long id;

    /** 收货人姓名 */
    private String consigneeName;

    /** 收货人手机号 */
    private String phone;

    /** 省份/直辖市 */
    private String province;

    /** 城市 */
    private String city;

    /** 区/县 */
    private String district;

    /** 详细地址（街道、门牌号等） */
    private String detailAddress;

    /** 是否为默认地址：0-否，1-是 */
    private Integer isDefault;
}
