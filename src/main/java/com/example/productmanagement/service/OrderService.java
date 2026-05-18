package com.example.productmanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.productmanagement.dto.*;
import com.example.productmanagement.vo.AdminOrderDetailVo;
import com.example.productmanagement.vo.AdminOrderListVo;
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

    /**
     * 用户确认收货
     * @param dto   请求参数
     */
    void userConfirmReceive(OrderOperateDto dto);

    /**
     * 用户取消订单
     */
    void userCancelOrder(OrderOperateDto dto);


    /**
     * 动态条件查询订单（分页）
     * 支持时间区间、商品类别、订单状态的组合筛选
     *
     * @param query 查询条件DTO
     * @return 分页订单列表
     */
    IPage<OrderListVo> queryOrders(OrderQueryDto query);

    /**
     * 动态条件查询订单（不分页）
     * 支持时间区间、商品类别、订单状态的组合筛选
     *
     * @param query 查询条件DTO
     * @return 订单列表
     */
    List<OrderListVo> queryOrdersNoPage(OrderQueryDto query);

    /**
     * 卖家发货处理：修改订单状态、通知用户
     * @param dto
     */
    void adminShipOrder(OrderOperateDto dto);

    IPage<AdminOrderListVo> adminListPaidOrders(Long adminId, Integer page, Integer pageSize);

    List<AdminOrderListVo> adminListPaidOrdersAll(Long adminId);

    AdminOrderDetailVo adminGetOrderDetail(Long adminId, String orderNo);

    IPage<AdminOrderListVo> adminListAllOrders(Long adminId, Integer page, Integer pageSize);

    List<AdminOrderListVo> adminListAllOrdersAll(Long adminId);

    IPage<AdminOrderListVo> adminListOrdersByStatus(Long adminId, Integer orderStatus, Integer page, Integer pageSize);

    List<AdminOrderListVo> adminListOrdersByStatusAll(Long adminId, Integer orderStatus);
}
