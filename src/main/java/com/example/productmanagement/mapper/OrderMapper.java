package com.example.productmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.productmanagement.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    IPage<Order> selectOrdersByUserId(@Param("page") Page<Order> page, @Param("userId") Long userId);

    List<Order> selectOrdersByUserId(@Param("userId") Long userId);

    Order selectByOrderNo(@Param("orderNo") String orderNo);

    int softDelete(@Param("orderId") Long orderId, @Param("userId") Long userId);
}

