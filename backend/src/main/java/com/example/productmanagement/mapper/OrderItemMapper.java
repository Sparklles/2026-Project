package com.example.productmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.example.productmanagement.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * 统计指定用户购买某本书的有效记录数 (只统计已支付或已完成的订单)
     */
    int countPurchasedRecord(@Param("userId") Long userId, @Param("bookId") Long bookId);

    int insertBatch(List<OrderItem> list);

    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);
}
