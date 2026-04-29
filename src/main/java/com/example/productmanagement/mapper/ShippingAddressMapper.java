package com.example.productmanagement.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.productmanagement.entity.ShippingAddress;
import org.apache.ibatis.annotations.Mapper;

/**
* @author freedom
* @description 针对表【shipping_address】的数据库操作Mapper
* @createDate 2026-04-28 15:33:26
* @Entity com.dujiang.sailmart.entity.ShippingAddress
*/
@Mapper
public interface ShippingAddressMapper extends BaseMapper<ShippingAddress> {

}




