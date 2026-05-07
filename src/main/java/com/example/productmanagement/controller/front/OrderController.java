package com.example.productmanagement.controller.front;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.productmanagement.common.Result;
import com.example.productmanagement.dto.OrderCreateRequest;
import com.example.productmanagement.dto.PayOrderRequest;
import com.example.productmanagement.dto.UpdateOrderStatus;
import com.example.productmanagement.service.OrderService;
import com.example.productmanagement.vo.OrderDetailVo;
import com.example.productmanagement.vo.OrderListVo;
import com.example.productmanagement.vo.OrderVo;
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
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<OrderListVo> orderPage = orderService.listOrdersByUserId(userId, page, pageSize);
        return Result.success(orderPage);
    }

    @GetMapping("/list/all")
    public Result<List<OrderListVo>> listAllOrders(@RequestParam Long userId) {
        List<OrderListVo> orderList = orderService.listOrdersByUserId(userId);
        return Result.success(orderList);
    }

    @PostMapping("/pay")
    public Result<Void> payOrder(@Valid @RequestBody PayOrderRequest request) {
        orderService.payOrder(request);
        return Result.success("支付成功");
    }

    @PutMapping("/status")
    public Result<Void> updateOrderStatus(@Valid @RequestBody UpdateOrderStatus request) {
        orderService.updateOrderStatus(request);
        return Result.success("更新成功");
    }

    @DeleteMapping("/delete/{orderNo}")
    public Result<Void> deleteOrder(
            @NotNull @PathVariable String orderNo,
            @NotNull @RequestParam Long userId) {
        orderService.deleteOrder(orderNo, userId);
        return Result.success("删除成功");
    }
}
