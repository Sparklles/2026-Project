package com.example.productmanagement.controller.front;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.productmanagement.common.Result;
import com.example.productmanagement.dto.*;
import com.example.productmanagement.service.OrderService;
import com.example.productmanagement.utils.UserHolder;
import com.example.productmanagement.vo.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 注意引入 java.util.Map 和 java.util.HashMap
    @PostMapping("/create")
    public Result<Map<String, String>> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        request.setUserId(UserHolder.getUserId());
        String orderNo = orderService.createOrder(request);

        // 🌟 核心修复：用 Map 包裹字符串，强制 Java 把它识别为 data 数据，而不是 message 提示！
        Map<String, String> resultData = new HashMap<>();
        resultData.put("orderNo", orderNo);

        return Result.success(resultData);
    }

    @GetMapping("/detail")
    public Result<OrderListVo> getOrderByOrderNo(@RequestParam String orderNo) {
        OrderListVo vo = orderService.getOrderByOrderNo(orderNo);
        return Result.success(vo);
    }

    @GetMapping("/detail/full")
    public Result<OrderDetailVo> getOrderDetail(@RequestParam String orderNo) {
        OrderDetailVo vo = orderService.getOrderDetail(orderNo);
        return Result.success(vo);
    }

    @GetMapping("/list")
    public Result<IPage<OrderListVo>> listOrders(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        userId = UserHolder.getUserId();
        IPage<OrderListVo> orderPage = orderService.listOrdersByUserId(userId, page, pageSize);
        return Result.success(orderPage);
    }

    @GetMapping("/list/all")
    public Result<List<OrderListVo>> listAllOrders(@RequestParam(required = false) Long userId) {
        userId = UserHolder.getUserId();
        List<OrderListVo> orderList = orderService.listOrdersByUserId(userId);
        return Result.success(orderList);
    }

    @PostMapping("/pay")
    public Result<Void> payOrder(@Valid @RequestBody PayOrderRequest request) {
        request.setUserId(UserHolder.getUserId());
        orderService.payOrder(request);
        return Result.success("支付成功");
    }

    /**
     * 用户确认收货
     * @return          操作结果
     */
    @PostMapping("/confirm-receive")
    public Result<?> userConfirmReceive(
            @RequestBody OrderOperateDto orderOperateDto
    ) {
        orderOperateDto.setUserId(UserHolder.getUserId());
        orderService.userConfirmReceive(orderOperateDto);
        return Result.success("确认收货成功");
    }

    /**
     * 用户取消订单
     * @param orderOperateDto
     * @return
     */
    @PostMapping("/cancel")
    public Result<?> userCancelOrder(@RequestBody OrderOperateDto orderOperateDto) {
        orderOperateDto.setUserId(UserHolder.getUserId());
        orderService.userCancelOrder(orderOperateDto);
        return Result.success("取消订单成功");
    }

    @PutMapping("/status")
    public Result<Void> updateOrderStatus(@Valid @RequestBody UpdateOrderStatus request) {
        request.setUserId(UserHolder.getUserId());
        orderService.updateOrderStatus(request);
        return Result.success("更新成功");
    }

    @DeleteMapping("/delete/{orderNo}")
    public Result<Void> deleteOrder(
            @NotNull @PathVariable String orderNo,
            @RequestParam(required = false) Long userId) {
        userId = UserHolder.getUserId();
        orderService.deleteOrder(orderNo, userId);
        return Result.success("删除成功");
    }

    /**
     * 动态条件查询订单（分页）
     * 支持时间区间、商品类别、订单状态的组合筛选
     *
     * @param query 查询条件DTO
     * @return 分页订单列表
     */
    @PostMapping("/query")
    public Result<IPage<OrderListVo>> queryOrders(@Valid @RequestBody OrderQueryDto query) {
        query.setUserId(UserHolder.getUserId());
        IPage<OrderListVo> orderPage = orderService.queryOrders(query);
        return Result.success(orderPage);
    }

    /**
     * 动态条件查询订单（不分页）
     * 支持时间区间、商品类别、订单状态的组合筛选
     *
     * @param query 查询条件DTO
     * @return 订单列表
     */
    @PostMapping("/query/all")
    public Result<List<OrderListVo>> queryOrdersNoPage(@Valid @RequestBody OrderQueryDto query) {
        query.setUserId(UserHolder.getUserId());
        List<OrderListVo> orderList = orderService.queryOrdersNoPage(query);
        return Result.success(orderList);
    }


    @PutMapping("/admin/ship")
    public Result<Void> adminShipOrder(@Valid @RequestBody OrderOperateDto dto) {
        dto.setUserId(UserHolder.getUserId());
        orderService.adminShipOrder(dto);
        return Result.success("发货成功");
    }

    @GetMapping("/admin/paid/list")
    public Result<IPage<AdminOrderListVo>> adminListPaidOrders(
            @RequestParam(required = false) Long adminId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(orderService.adminListPaidOrders(UserHolder.getUserId(), page, pageSize));
    }

    @GetMapping("/admin/paid/list/all")
    public Result<List<AdminOrderListVo>> adminListPaidOrdersAll(
            @RequestParam(required = false) Long adminId) {
        return Result.success(orderService.adminListPaidOrdersAll(UserHolder.getUserId()));
    }

    @GetMapping("/admin/detail")
    public Result<AdminOrderDetailVo> adminGetOrderDetail(
            @RequestParam(required = false) Long adminId,
            @NotNull @RequestParam String orderNo) {
        return Result.success(orderService.adminGetOrderDetail(UserHolder.getUserId(), orderNo));
    }

    @GetMapping("/admin/list")
    public Result<IPage<AdminOrderListVo>> adminListAllOrders(
            @RequestParam(required = false) Long adminId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(orderService.adminListAllOrders(UserHolder.getUserId(), page, pageSize));
    }

    @GetMapping("/admin/list/all")
    public Result<List<AdminOrderListVo>> adminListAllOrdersAll(
            @RequestParam(required = false) Long adminId) {
        return Result.success(orderService.adminListAllOrdersAll(UserHolder.getUserId()));
    }

    @GetMapping("/admin/status/list")
    public Result<IPage<AdminOrderListVo>> adminListOrdersByStatus(
            @RequestParam(required = false) Long adminId,
            @NotNull @RequestParam Integer orderStatus,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(orderService.adminListOrdersByStatus(UserHolder.getUserId(), orderStatus, page, pageSize));
    }

    @GetMapping("/admin/status/list/all")
    public Result<List<AdminOrderListVo>> adminListOrdersByStatusAll(
            @RequestParam(required = false) Long adminId,
            @NotNull @RequestParam Integer orderStatus) {
        return Result.success(orderService.adminListOrdersByStatusAll(UserHolder.getUserId(), orderStatus));
    }
}

