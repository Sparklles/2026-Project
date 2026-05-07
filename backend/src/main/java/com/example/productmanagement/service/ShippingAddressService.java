package com.example.productmanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.productmanagement.dto.ShippingAddressDto;
import com.example.productmanagement.entity.ShippingAddress;
import com.example.productmanagement.vo.ShippingAddressVo;

import java.util.List;

/**
 * @author freedom
 * @description 针对表【shipping_address】的数据库操作 Service
 * @createDate 2026-04-28 15:33:26
 */
public interface ShippingAddressService extends IService<ShippingAddress> {

    /**
     * 查询当前登录用户的全部收货地址（默认地址排在最前面）。
     *
     * @return 地址列表
     */
    List<ShippingAddressVo> listMyAddresses();

    /**
     * 为当前登录用户新增一条收货地址。
     * 若 dto.isDefault == 1，会先将该用户其余地址的默认标志置 0。
     *
     * @param dto 前端传入的地址信息
     */
    void addAddress(ShippingAddressDto dto);

    /**
     * 删除当前登录用户的指定收货地址。
     * 仅允许操作属于自己的地址，否则抛出业务异常。
     *
     * @param addressId 地址主键 ID
     */
    void deleteAddress(Long addressId);

    /**
     * 修改当前登录用户的指定收货地址。
     * 仅允许操作属于自己的地址，否则抛出业务异常。
     * 若 dto.isDefault == 1，会先将该用户其余地址的默认标志置 0。
     *
     * @param addressId 地址主键 ID
     * @param dto       需要更新的地址信息
     */
    void updateAddress(Long addressId, ShippingAddressDto dto);

    /**
     * 将指定地址设为当前登录用户的默认地址（其余地址自动取消默认）。
     *
     * @param addressId 地址主键 ID
     */
    void setDefaultAddress(Long addressId);
}
