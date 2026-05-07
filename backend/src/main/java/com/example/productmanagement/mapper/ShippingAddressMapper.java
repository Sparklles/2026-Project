package com.example.productmanagement.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.productmanagement.entity.ShippingAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author freedom
* @description 针对表【shipping_address】的数据库操作Mapper
* @createDate 2026-04-28 15:33:26
* @Entity com.dujiang.sailmart.entity.ShippingAddress
*/
@Mapper
public interface ShippingAddressMapper extends BaseMapper<ShippingAddress> {
    // 🌟 1. 修复生成订单时查询单个地址的 SQL (去掉 bookshop，改用 shipping_address)
    @Select("SELECT * FROM shipping_address WHERE user_id = #{userId} AND id = #{id}")
    ShippingAddress getAddressBydUserIdAndId(@Param("userId") Long userId, @Param("id") Long id);

    // 🌟 2. 新增：获取用户的所有地址列表 (按是否默认、更新时间倒序)
    @Select("SELECT * FROM shipping_address WHERE user_id = #{userId} ORDER BY is_default DESC, update_time DESC")
    List<ShippingAddress> getAddressListByUserId(@Param("userId") Long userId);
}




