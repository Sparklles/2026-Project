package com.example.productmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.productmanagement.dto.ShippingAddressDto;
import com.example.productmanagement.entity.ShippingAddress;
import com.example.productmanagement.exception.BizIllegalException;
import com.example.productmanagement.mapper.ShippingAddressMapper;
import com.example.productmanagement.result.ResultCodeEnum;
import com.example.productmanagement.service.ShippingAddressService;
import com.example.productmanagement.utils.UserHolder;
import com.example.productmanagement.vo.ShippingAddressVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author freedom
 * @description 针对表【shipping_address】的数据库操作 Service 实现
 * @createDate 2026-04-28 15:33:26
 */
@Service
public class ShippingAddressServiceImpl extends ServiceImpl<ShippingAddressMapper, ShippingAddress>
        implements ShippingAddressService {

    // ----------------------------------------------------------------
    //  公开业务接口实现
    // ----------------------------------------------------------------

    @Override
    public List<ShippingAddressVo> listMyAddresses() {
        Long userId = UserHolder.getUserId();

        // 按"默认在前、创建时间倒序"排列
        List<ShippingAddress> addresses = lambdaQuery()
                .eq(ShippingAddress::getUserId, userId)
                .orderByDesc(ShippingAddress::getIsDefault)
                .orderByDesc(ShippingAddress::getCreateTime)
                .list();

        return addresses.stream()
                .map(this::toVo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAddress(ShippingAddressDto dto) {
        validateDto(dto);

        Long userId = UserHolder.getUserId();

        // 若新增地址设为默认，先清除其他默认地址
        if (Integer.valueOf(1).equals(dto.getIsDefault())) {
            clearDefault(userId);
        }

        ShippingAddress address = new ShippingAddress();
        address.setUserId(userId);
        fillFromDto(address, dto);

        save(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAddress(Long addressId) {
        ShippingAddress address = getOwnedAddress(addressId);
        removeById(address.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(Long addressId, ShippingAddressDto dto) {
        validateDto(dto);

        ShippingAddress address = getOwnedAddress(addressId);
        Long userId = address.getUserId();

        // 若修改后设为默认，先清除同用户其他默认地址
        if (Integer.valueOf(1).equals(dto.getIsDefault())) {
            clearDefault(userId);
        }

        fillFromDto(address, dto);
        updateById(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultAddress(Long addressId) {
        ShippingAddress address = getOwnedAddress(addressId);
        Long userId = address.getUserId();

        // 先将该用户所有地址设为非默认
        clearDefault(userId);

        // 再将目标地址设为默认
        LambdaUpdateWrapper<ShippingAddress> wrapper = new LambdaUpdateWrapper<ShippingAddress>()
                .eq(ShippingAddress::getId, addressId)
                .set(ShippingAddress::getIsDefault, 1);
        update(wrapper);
    }

    // ----------------------------------------------------------------
    //  私有工具方法
    // ----------------------------------------------------------------

    /**
     * 校验地址 DTO 必填字段，缺失时抛出业务异常。
     */
    private void validateDto(ShippingAddressDto dto) {
        if (dto.getConsigneeName() == null || dto.getConsigneeName().isBlank()) {
            throw new BizIllegalException(ResultCodeEnum.PARAM_ERROR);
        }
        if (dto.getPhone() == null || dto.getPhone().isBlank()) {
            throw new BizIllegalException(ResultCodeEnum.PARAM_ERROR);
        }
        if (dto.getProvince() == null || dto.getProvince().isBlank()) {
            throw new BizIllegalException(ResultCodeEnum.PARAM_ERROR);
        }
        if (dto.getCity() == null || dto.getCity().isBlank()) {
            throw new BizIllegalException(ResultCodeEnum.PARAM_ERROR);
        }
        if (dto.getDistrict() == null || dto.getDistrict().isBlank()) {
            throw new BizIllegalException(ResultCodeEnum.PARAM_ERROR);
        }
        if (dto.getDetailAddress() == null || dto.getDetailAddress().isBlank()) {
            throw new BizIllegalException(ResultCodeEnum.PARAM_ERROR);
        }
    }

    /**
     * 获取属于当前登录用户的地址，若不存在或不属于该用户则抛出异常。
     */
    private ShippingAddress getOwnedAddress(Long addressId) {
        Long userId = UserHolder.getUserId();
        ShippingAddress address = getById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BizIllegalException(ResultCodeEnum.ILLEGAL_REQUEST);
        }
        return address;
    }

    /**
     * 将指定用户所有地址的默认标志清零。
     */
    private void clearDefault(Long userId) {
        LambdaUpdateWrapper<ShippingAddress> wrapper = new LambdaUpdateWrapper<ShippingAddress>()
                .eq(ShippingAddress::getUserId, userId)
                .set(ShippingAddress::getIsDefault, 0);
        update(wrapper);
    }

    /**
     * 将 DTO 字段写入实体（供新增和修改共用）。
     */
    private void fillFromDto(ShippingAddress address, ShippingAddressDto dto) {
        address.setConsigneeName(dto.getConsigneeName());
        address.setPhone(dto.getPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetailAddress(dto.getDetailAddress());
        address.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : 0);
    }

    /**
     * 实体 → VO 转换。
     */
    private ShippingAddressVo toVo(ShippingAddress address) {
        ShippingAddressVo vo = new ShippingAddressVo();
        vo.setId(address.getId());
        vo.setConsigneeName(address.getConsigneeName());
        vo.setPhone(address.getPhone());
        vo.setProvince(address.getProvince());
        vo.setCity(address.getCity());
        vo.setDistrict(address.getDistrict());
        vo.setDetailAddress(address.getDetailAddress());
        vo.setIsDefault(address.getIsDefault());
        return vo;
    }
}
