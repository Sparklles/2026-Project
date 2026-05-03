package com.example.productmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.example.productmanagement.common.ErrorCode;
import com.example.productmanagement.common.StatusEnum;
import com.example.productmanagement.dto.OrderCreateRequest;
import com.example.productmanagement.dto.OrderItemDTO;
import com.example.productmanagement.dto.PayOrderRequest;
import com.example.productmanagement.dto.UpdateOrderStatus;
import com.example.productmanagement.entity.*;
import com.example.productmanagement.exception.BusinessException;
import com.example.productmanagement.mapper.*;
import com.example.productmanagement.service.OrderService;
import com.example.productmanagement.vo.OrderDetailVo;
import com.example.productmanagement.vo.OrderItemVo;
import com.example.productmanagement.vo.OrderListVo;
import com.example.productmanagement.vo.OrderVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ShippingAddressMapper addressMapper;
    private final BookInfoMapper bookInfoMapper;
    private final UserMapper userMapper;


    public OrderServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper, ShippingAddressMapper addressMapper, BookInfoMapper bookInfoMapper, UserMapper userMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.addressMapper = addressMapper;
        this.bookInfoMapper = bookInfoMapper;
        this.userMapper = userMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String createOrder(OrderCreateRequest request) { // 🌟 确保返回值是 String

        Long userId = request.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), ErrorCode.USER_NOT_FOUND.getMessage());
        }
        String username = user.getLoginAccount();

        ShippingAddress address = addressMapper.getAddressBydUserIdAndId(userId, request.getAddressId());
        if (address == null) {
            throw new BusinessException(ErrorCode.USER_ADDRESS_NOT_FOUND.getCode(), ErrorCode.USER_ADDRESS_NOT_FOUND.getMessage());
        }

        List<OrderItemDTO> orderItems = request.getOrderItems();
        if (CollectionUtils.isEmpty(orderItems)) {
            throw new BusinessException(400, "订单商品不能为空");
        }

        List<Long> bookIds = orderItems.stream()
                .map(OrderItemDTO::getBookId)
                .collect(Collectors.toList());

        List<BookInfo> bookList = bookInfoMapper.selectBatchIds(bookIds);

        String orderNo = IdWorker.getIdStr();
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;

        // 1. 安全计算总价
        for (OrderItemDTO orderItem : orderItems) {
            BookInfo bookInfo = bookList.stream()
                    .filter(b -> b.getId().equals(orderItem.getBookId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(400, "抱歉，部分商品不存在或已下架")); // 🌟 防空指针

            BigDecimal price = bookInfo.getPrice() != null ? bookInfo.getPrice() : BigDecimal.ZERO;
            Integer qty = orderItem.getQuantity() != null ? orderItem.getQuantity() : 1;
            BigDecimal itemTotal = price.multiply(new BigDecimal(qty));

            totalAmount = totalAmount.add(itemTotal);
        }

        BigDecimal payAmount = totalAmount.subtract(discountAmount);

        // 2. 插入订单主表
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setUsername(username);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(payAmount);
        order.setDiscountAmount(discountAmount);
        order.setConsignee(address.getConsigneeName());
        order.setPhone(address.getPhone());
        order.setAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetailAddress());
        order.setOrderStatus(StatusEnum.OrderStatus.PENDING_PAY.getCode());
        order.setPayStatus(StatusEnum.PayStatus.UNPAID.getCode());
        order.setRemark(request.getRemark());

        orderMapper.insert(order);

        // 3. 插入订单详情 (🌟 抛弃隐患极大的自定义 insertBatch，直接循环使用原生的 insert)
        for (OrderItemDTO item : orderItems) {
            BookInfo book = bookList.stream()
                    .filter(b -> b.getId().equals(item.getBookId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(400, "抱歉，部分商品不存在或已下架"));

            OrderItem orderItem = new OrderItem();

            // 🌟 极端防错：万一主表 ID 没生成回来，提前拦截
            if (order.getOrderId() == null) {
                throw new BusinessException(500, "订单主键生成失败，请检查数据库配置");
            }

            orderItem.setOrderId(order.getOrderId());
            orderItem.setBookId(book.getId());
            orderItem.setBookTitle(book.getTitle());
            orderItem.setCoverImageUrl(book.getCoverImageUrl());
            orderItem.setPrice(book.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setTotalPrice(book.getPrice().multiply(new BigDecimal(item.getQuantity())));

            // 使用 MyBatis-Plus 内置的单条插入，完美避开 XML 写错引发的报错！
            orderItemMapper.insert(orderItem);
        }

        // 4. 返回生成的真实订单号给前端
        return orderNo;
    }

    @Override
    public IPage<OrderListVo> listOrdersByUserId(Long userId, Integer page, Integer pageSize) {
        Page<Order> pageParam = new Page<>(page, pageSize);
        IPage<Order> orderPage = orderMapper.selectOrdersByUserId(pageParam, userId);

        if (orderPage.getTotal() <= 0 || CollectionUtils.isEmpty(orderPage.getRecords())) {
            Page<OrderListVo> emptyPage = new Page<>(page, pageSize);
            emptyPage.setTotal(0);
            return emptyPage;
        }

        List<Order> orders = orderPage.getRecords();
        Map<Long, List<OrderItem>> itemMap = loadOrderItems(orders);

        List<OrderListVo> voList = orders.stream()
                .map(order -> toOrderListVo(order, itemMap.getOrDefault(order.getOrderId(), List.of())))
                .collect(Collectors.toList());

        Page<OrderListVo> resultPage = new Page<>(orderPage.getCurrent(), orderPage.getSize());
        resultPage.setRecords(voList);
        resultPage.setTotal(orderPage.getTotal());

        return resultPage;
    }

    @Override
    public List<OrderListVo> listOrdersByUserId(Long userId) {
        List<Order> orders = orderMapper.selectOrdersByUserId(userId);

        if (CollectionUtils.isEmpty(orders)) {
            return List.of();
        }

        Map<Long, List<OrderItem>> itemMap = loadOrderItems(orders);

        return orders.stream()
                .map(order -> toOrderListVo(order, itemMap.getOrDefault(order.getOrderId(), List.of())))
                .collect(Collectors.toList());
    }

    @Override
    public OrderListVo getOrderByOrderNo(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND.getCode(),
                    ErrorCode.ORDER_NOT_FOUND.getMessage());
        }

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, order.getOrderId()));

        return toOrderListVo(order, items);
    }

    @Override
    public OrderDetailVo getOrderDetail(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND.getCode(),
                    ErrorCode.ORDER_NOT_FOUND.getMessage());
        }

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, order.getOrderId()));

        return toOrderDetailVo(order, items);
    }

    @Transactional
    @Override
    public void payOrder(PayOrderRequest request) {
        // 查询订单是否存在
        Order order = orderMapper.selectByOrderNo(request.getOrderNo());
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND.getCode(),
                    ErrorCode.ORDER_NOT_FOUND.getMessage());
        }

        // 查询订单是否与用户匹配
        if (!order.getUserId().equals(request.getUserId())) {
            throw new BusinessException(ErrorCode.ORDER_NO_PERMISSION.getCode(),
                    ErrorCode.ORDER_NO_PERMISSION.getMessage());
        }

        // 订单不是待支付状态
        if (!order.getOrderStatus().equals(StatusEnum.OrderStatus.PENDING_PAY.getCode())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID.getCode(),
                    ErrorCode.ORDER_STATUS_INVALID.getMessage());
        }

        // 校验书籍状态和库存，并扣减库存
        List<OrderItem> items = orderItemMapper.selectByOrderId(order.getOrderId());
        for (OrderItem item : items) {
            BookInfo book = bookInfoMapper.selectById(item.getBookId());
            if (book == null) {
                throw new BusinessException(ErrorCode.BOOK_NOT_FOUND.getCode(),
                        ErrorCode.BOOK_NOT_FOUND.getMessage());
            }
            // 校验书籍是否上架
            if (book.getStatus() == null || book.getStatus() != 1) {
                throw new BusinessException(ErrorCode.ORDER_BOOK_OFF_SHELF.getCode(),
                        "商品【" + book.getTitle() + "】已下架");
            }
            // 校验库存是否充足
            if (book.getStock() == null || book.getStock() < item.getQuantity()) {
                throw new BusinessException(ErrorCode.ORDER_STOCK_INSUFFICIENT.getCode(),
                        "商品【" + book.getTitle() + "】库存不足");
            }
            bookInfoMapper.decreaseStock(item.getBookId(), item.getQuantity());
        }

        order.setPayType(request.getPayType());
        order.setPayStatus(StatusEnum.PayStatus.PAYED.getCode());
        order.setPayTime(LocalDateTime.now());
        order.setOrderStatus(StatusEnum.OrderStatus.PENDING_SHIP.getCode());
        order.setUpdateTime(LocalDateTime.now());

        orderMapper.updateById(order);
    }

    @Transactional
    @Override
    public void updateOrderStatus(UpdateOrderStatus request) {
        // 获取订单
        Order order = orderMapper.selectByOrderNo(request.getOrderNo());
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND.getCode(),
                    ErrorCode.ORDER_NOT_FOUND.getMessage());
        }
        
        // 订单鉴权
        if (!order.getUserId().equals(request.getUserId())) {
            throw new BusinessException(ErrorCode.ORDER_NO_PERMISSION.getCode(),
                    ErrorCode.ORDER_NO_PERMISSION.getMessage());
        }
        
        int newStatus = request.getOrderStatus();
        int currentStatus = order.getOrderStatus();

        // 订单状态转移不合法
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID.getCode(),
                    ErrorCode.ORDER_STATUS_INVALID.getMessage());
        }

        order.setOrderStatus(newStatus);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Transactional
    @Override
    public void deleteOrder(String orderNo, Long userId) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND.getCode(),
                    ErrorCode.ORDER_NOT_FOUND.getMessage());
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_NO_PERMISSION.getCode(),
                    ErrorCode.ORDER_NO_PERMISSION.getMessage());
        }

        int rows = orderMapper.softDelete(order.getOrderId(), userId);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND.getCode(),
                    ErrorCode.ORDER_NOT_FOUND.getMessage());
        }
    }

    /**
     * 判断订单状态流转是否合法
     * 1-待支付 → 2-待发货 / 5-已取消
     * 2-待发货 → 3-已发货 / 6-售后中
     * 3-已发货 → 7-待签收 / 6-售后中
     * 7-待签收 → 4-已完成
     * 6-售后中 → 8-已退款
     */
    private boolean isValidStatusTransition(int current, int next) {
        return switch (current) {
            case 1 -> next == 2 || next == 5;
            case 2 -> next == 3 || next == 6;
            case 3 -> next == 7 || next == 6;
            case 7 -> next == 4;
            case 6 -> next == 8;
            default -> false;
        };
    }

    private Map<Long, List<OrderItem>> loadOrderItems(List<Order> orders) {
        List<Long> orderIds = orders.stream()
                .map(Order::getOrderId)
                .collect(Collectors.toList());

        List<OrderItem> allItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));

        return allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));
    }

    private OrderListVo toOrderListVo(Order order, List<OrderItem> items) {
        OrderListVo vo = new OrderListVo();
        vo.setOrderId(order.getOrderId());
        vo.setOrderNo(order.getOrderNo());
        vo.setOrderStatus(order.getOrderStatus());
        vo.setPayStatus(order.getPayStatus());
        vo.setTotalPrice(order.getTotalAmount());
        vo.setDiscountPrice(order.getDiscountAmount());
        vo.setPayPrice(order.getPayAmount());
        vo.setCreateTime(order.getCreateTime());

        List<OrderItemVo> itemVos = items.stream().map(item -> {
            OrderItemVo iv = new OrderItemVo();
            iv.setBookId(item.getBookId().intValue());
            iv.setBookTitle(item.getBookTitle());
            iv.setCoverUrl(item.getCoverImageUrl());
            iv.setQuantity(item.getQuantity());
            iv.setPrice(item.getPrice());

            iv.setDiscount(BigDecimal.ZERO);
            return iv;
        }).collect(Collectors.toList());

        vo.setItems(itemVos);
        return vo;
    }

    private OrderDetailVo toOrderDetailVo(Order order, List<OrderItem> items) {
        OrderDetailVo vo = new OrderDetailVo();
        vo.setOrderId(order.getOrderId());
        vo.setOrderNo(order.getOrderNo());
        vo.setOrderStatus(order.getOrderStatus());
        vo.setPayStatus(order.getPayStatus());
        vo.setTotalPrice(order.getTotalAmount());
        vo.setDiscountPrice(order.getDiscountAmount());
        vo.setPayPrice(order.getPayAmount());
        vo.setCreateTime(order.getCreateTime());
        vo.setPayTime(order.getPayTime());
        vo.setConsignee(order.getConsignee());
        vo.setPhone(order.getPhone());
        vo.setAddress(order.getAddress());
        vo.setRemark(order.getRemark());
        vo.setPayType(order.getPayType());

        List<OrderItemVo> itemVos = items.stream().map(item -> {
            OrderItemVo iv = new OrderItemVo();
            iv.setBookId(item.getBookId().intValue());
            iv.setBookTitle(item.getBookTitle());
            iv.setCoverUrl(item.getCoverImageUrl());
            iv.setQuantity(item.getQuantity());
            iv.setPrice(item.getPrice());
            iv.setDiscount(BigDecimal.ZERO);
            return iv;
        }).collect(Collectors.toList());

        vo.setItems(itemVos);
        return vo;
    }

    private OrderVo convertToVo(Order order) {
        OrderVo vo = new OrderVo();
        vo.setOrderId(order.getOrderId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setUsername(order.getUsername());
        vo.setOrderStatus(order.getOrderStatus());
        vo.setPayStatus(order.getPayStatus());
        vo.setPayType(order.getPayType());
        vo.setPayTime(order.getPayTime());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setDiscountAmount(order.getDiscountAmount());
        vo.setConsignee(order.getConsignee());
        vo.setPhone(order.getPhone());
        vo.setAddress(order.getAddress());
        vo.setRemark(order.getRemark());
        vo.setCreateTime(order.getCreateTime());
        vo.setUpdateTime(order.getUpdateTime());
        return vo;
    }
}
