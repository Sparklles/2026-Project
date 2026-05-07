package com.example.productmanagement.controller.profile;


import com.example.productmanagement.dto.ShippingAddressDto;
import com.example.productmanagement.entity.ShippingAddress;
import com.example.productmanagement.mapper.ShippingAddressMapper;
import com.example.productmanagement.result.Result;
import com.example.productmanagement.service.ShippingAddressService;
import com.example.productmanagement.utils.UserHolder;
import com.example.productmanagement.vo.ShippingAddressVo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端 - 收货地址控制器
 *
 * <p>所有接口需携带有效 Token（role=1 普通用户），由拦截器统一鉴权。
 *
 * <pre>
 * GET    /api/address/list              查询当前用户的全部收货地址
 * POST   /api/address                   新增收货地址
 * DELETE /api/address/{id}             删除指定收货地址
 * PUT    /api/address/{id}             修改指定收货地址
 * PUT    /api/address/{id}/default     将指定地址设为默认
 * </pre>
 */
@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class ShippingAddressController {

    private final ShippingAddressService shippingAddressService;
    @Autowired
    private ShippingAddressMapper shippingAddressMapper;

    /**
     * 查询当前登录用户的全部收货地址。
     * 默认地址排在列表最前面，其余按创建时间倒序排列。
     */
    @GetMapping("/list")
    public Result<List<ShippingAddressVo>> getAddressList() {
        return Result.ok(shippingAddressService.listMyAddresses());
    }

    /**
     * 新增收货地址。
     * 若 isDefault=1，系统会自动将当前用户其余地址的默认标志清零。
     */
    @PostMapping
    public Result<Void> addAddress(@RequestBody ShippingAddressDto dto) {
        shippingAddressService.addAddress(dto);
        return Result.ok();
    }

    /**
     * 删除指定收货地址。
     * 仅允许删除属于当前登录用户的地址，否则返回非法请求错误。
     *
     * @param id 地址主键 ID
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id) {
        shippingAddressService.deleteAddress(id);
        return Result.ok();
    }

    /**
     * 修改指定收货地址。
     * 仅允许修改属于当前登录用户的地址，否则返回非法请求错误。
     * 若 isDefault=1，系统会自动将当前用户其余地址的默认标志清零。
     *
     * @param id  地址主键 ID
     * @param dto 修改后的地址信息
     */
    @PutMapping("/{id}")
    public Result<Void> updateAddress(@PathVariable Long id, @RequestBody ShippingAddressDto dto) {
        shippingAddressService.updateAddress(id, dto);
        return Result.ok();
    }

    /**
     * 将指定地址设为当前登录用户的默认地址。
     * 其余地址的默认标志会被自动清零，保证同一用户只存在一个默认地址。
     *
     * @param id 地址主键 ID
     */
    @PutMapping("/default/{id}")
    public Result<Void> setDefaultAddress(@PathVariable Long id) {
        shippingAddressService.setDefaultAddress(id);
        return Result.ok();
    }

    /**
     * 获取当前用户的收货地址列表
     */
    @GetMapping("/list2")
    public com.example.productmanagement.controller.Result<List<ShippingAddress>> listAddresses(@RequestParam Long userId) {
        Long userId1 = UserHolder.getUserId();
        userId=userId1;
        List<ShippingAddress> list = shippingAddressMapper.getAddressListByUserId(userId);
        return com.example.productmanagement.controller.Result.success(list);
    }
}
