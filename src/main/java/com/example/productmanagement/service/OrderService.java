package com.example.productmanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.productmanagement.dto.OrderCreateRequest;
import com.example.productmanagement.dto.PayOrderRequest;
import com.example.productmanagement.dto.UpdateOrderStatus;
import com.example.productmanagement.vo.OrderDetailVo;
import com.example.productmanagement.vo.OrderListVo;


import java.util.List;

public interface OrderService {

    String createOrder(OrderCreateRequest request);

    IPage<OrderListVo> listOrdersByUserId(Long userId, Integer page, Integer pageSize);

    List<OrderListVo> listOrdersByUserId(Long userId);

    OrderListVo getOrderByOrderNo(String orderNo);

    OrderDetailVo getOrderDetail(String orderNo);

    void payOrder(PayOrderRequest request);

    void updateOrderStatus(UpdateOrderStatus request);

    void deleteOrder(String orderNo, Long userId);
}
