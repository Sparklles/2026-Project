package com.example.productmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.example.productmanagement.common.ErrorCode;
import com.example.productmanagement.common.StatusEnum;
import com.example.productmanagement.dto.*;
import com.example.productmanagement.entity.*;
import com.example.productmanagement.exception.BusinessException;
import com.example.productmanagement.mapper.*;
import com.example.productmanagement.service.NotificationService;
import com.example.productmanagement.service.OrderService;
import com.example.productmanagement.service.UserBehaviorLogService;
import com.example.productmanagement.utils.UserHolder;
import com.example.productmanagement.vo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;


@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ShippingAddressMapper addressMapper;
    private final BookInfoMapper bookInfoMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final UserBehaviorLogService userBehaviorLogService;

    public OrderServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper, ShippingAddressMapper addressMapper, BookInfoMapper bookInfoMapper, UserMapper userMapper, NotificationService notificationService, UserBehaviorLogService userBehaviorLogService) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.addressMapper = addressMapper;
        this.bookInfoMapper = bookInfoMapper;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
        this.userBehaviorLogService = userBehaviorLogService;
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
            orderItem.setDiscountAmount(BigDecimal.ZERO);           // 暂定折扣价为零
            orderItem.setPayAmount(order.getPayAmount().subtract(orderItem.getDiscountAmount()));

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

        validateCurrentUserOrder(order);

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

        validateCurrentUserOrder(order);

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

        // 校验支付方式
        if (request.getPayType()<1||request.getPayType()>3){
            throw new BusinessException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORT.getCode(),
                    ErrorCode.PAYMENT_METHOD_NOT_SUPPORT.getMessage());
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
        order.setPayStatus(StatusEnum.PayStatus.PAID.getCode());
        order.setPayTime(LocalDateTime.now());
        order.setOrderStatus(StatusEnum.OrderStatus.PENDING_SHIP.getCode());
        order.setUpdateTime(LocalDateTime.now());

        orderMapper.updateById(order);

        // 通知卖家发货
        notificationService.notifySellerOrderPaid(request.getOrderNo(), request.getUserId());

        // 支付成功后按订单明细逐本书记录购买行为，供推荐模块使用。
        for (OrderItem item : items) {
            userBehaviorLogService.recordPurchase(order.getUserId(), item.getBookId());
        }
    }

    @Transactional
    @Override
    public void userConfirmReceive(OrderOperateDto dto) {
        UpdateOrderStatus request = new UpdateOrderStatus();
        request.setUserId(dto.getUserId());
        request.setOrderNo(dto.getOrderNo());
        request.setOrderStatus(StatusEnum.OrderStatus.COMPLETED.getCode());

        // 调用核心方法，并通过 Lambda 表达式注入自定义更新逻辑
        updateOrderStatus(request, order -> {
            // 在这里直接操作已经通过校验的 Order 对象
            // 假设你的实体类中完成时间字段名为 completeTime
            order.setCloseTime(LocalDateTime.now());
        });

        // 通知卖家订单已完成
        notificationService.notifySellerOrderCompleted(dto.getOrderNo(), dto.getUserId());
    }

    @Transactional
    @Override
    public void userCancelOrder(OrderOperateDto dto) {
        UpdateOrderStatus request = new UpdateOrderStatus();
        request.setUserId(dto.getUserId());
        request.setOrderNo(dto.getOrderNo());
        request.setOrderStatus(StatusEnum.OrderStatus.CANCELED.getCode());

        // 调用核心方法，并通过 Lambda 表达式注入自定义更新逻辑
        updateOrderStatus(request, order -> {
            // 在这里直接操作已经通过校验的 Order 对象
            // 假设你的实体类中完成时间字段名为 completeTime
            order.setCancelTime(LocalDateTime.now());
            order.setCloseTime(LocalDateTime.now());
        });

        // 通知卖家订单已取消
        notificationService.notifySellerOrderCancelled(dto.getOrderNo(), dto.getUserId());
    }

    @Transactional
    @Override
    public void updateOrderStatus(UpdateOrderStatus request) {
        this.updateOrderStatus(request, null);
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

    @Transactional
    public void updateOrderStatus(UpdateOrderStatus request, Consumer<Order> customOrderUpdater) {
        // 1. 获取订单
        Order order = orderMapper.selectByOrderNo(request.getOrderNo());
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND.getCode(),
                    ErrorCode.ORDER_NOT_FOUND.getMessage());
        }

        // 2. 订单鉴权
        if (!order.getUserId().equals(request.getUserId())) {
            throw new BusinessException(ErrorCode.ORDER_NO_PERMISSION.getCode(),
                    ErrorCode.ORDER_NO_PERMISSION.getMessage());
        }

        int newStatus = request.getOrderStatus();
        int currentStatus = order.getOrderStatus();

        // 3. 订单状态转移不合法拦截
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID.getCode(),
                    ErrorCode.ORDER_STATUS_INVALID.getMessage());
        }

        // 4. 设置通用的状态和更新时间
        order.setOrderStatus(newStatus);
        order.setUpdateTime(LocalDateTime.now());

        // 5. 【核心优化点】执行调用方传入的自定义额外属性修改逻辑
        if (customOrderUpdater != null) {
            customOrderUpdater.accept(order);
        }

        // 6. 统一执行一次落库
        orderMapper.updateById(order);
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
            case 3 -> next == 7 || next == 6 || next == 4;
            case 7 -> next == 4;
            case 6 -> next == 8;
            default -> false;
        };
    }

    private void validateCurrentUserOrder(Order order) {
        Long userId = UserHolder.getUserId();
        if (!Objects.equals(order.getUserId(), userId)) {
            throw new BusinessException(ErrorCode.ORDER_NO_PERMISSION.getCode(),
                    ErrorCode.ORDER_NO_PERMISSION.getMessage());
        }
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
        vo.setFreightPrice(order.getFreightAmount());

        List<OrderItemVo> itemVos = items.stream().map(item -> {
            OrderItemVo iv = new OrderItemVo();
            iv.setOrderItemId(item.getId());
            iv.setBookId(item.getBookId().intValue());
            iv.setBookTitle(item.getBookTitle());
            iv.setCoverUrl(item.getCoverImageUrl());
            iv.setQuantity(item.getQuantity());
            iv.setPrice(item.getPrice());

            iv.setDiscount(item.getDiscountAmount());
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
        vo.setShipTime(order.getDeliveryTime());
        vo.setCloseTime(order.getCloseTime());
        vo.setFreightPrice(order.getFreightAmount());
        vo.setConsignee(order.getConsignee());
        vo.setPhone(order.getPhone());
        vo.setAddress(order.getAddress());
        vo.setRemark(order.getRemark());
        vo.setPayType(order.getPayType());

        List<OrderItemVo> itemVos = items.stream().map(item -> {
            OrderItemVo iv = new OrderItemVo();
            iv.setBookId(item.getBookId().intValue());
            iv.setOrderItemId(item.getId());
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

    @Override
    public IPage<OrderListVo> queryOrders(OrderQueryDto query) {
        validateQueryParams(query);

        Page<OrderListVo> pageParam = new Page<>(query.getCurrent(), query.getSize());
        IPage<OrderListVo> orderPage = orderMapper.selectOrdersByDynamicQuery(pageParam, query);

        if (orderPage.getTotal() <= 0 || CollectionUtils.isEmpty(orderPage.getRecords())) {
            Page<OrderListVo> emptyPage = new Page<>(query.getCurrent(), query.getSize());
            emptyPage.setTotal(0);
            return emptyPage;
        }

        List<OrderListVo> orders = orderPage.getRecords();
        List<Long> orderIds = orders.stream()
                .map(OrderListVo::getOrderId)
                .collect(Collectors.toList());

        List<OrderItem> allItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));

        Map<Long, List<OrderItem>> itemMap = allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        orders.forEach(order -> {
            List<OrderItem> items = itemMap.getOrDefault(order.getOrderId(), List.of());
            List<OrderItemVo> itemVos = items.stream().map(item -> {
                OrderItemVo iv = new OrderItemVo();
                iv.setBookId(item.getBookId().intValue());
                iv.setOrderItemId(item.getId());
                iv.setBookTitle(item.getBookTitle());
                iv.setCoverUrl(item.getCoverImageUrl());
                iv.setQuantity(item.getQuantity());
                iv.setPrice(item.getPrice());
                iv.setDiscount(BigDecimal.ZERO);
                return iv;
            }).collect(Collectors.toList());
            order.setItems(itemVos);
        });

        return orderPage;
    }

    @Override
    public List<OrderListVo> queryOrdersNoPage(OrderQueryDto query) {
        validateQueryParams(query);

        List<OrderListVo> orders = orderMapper.selectOrdersByDynamicQuery(query);

        if (CollectionUtils.isEmpty(orders)) {
            return List.of();
        }

        List<Long> orderIds = orders.stream()
                .map(OrderListVo::getOrderId)
                .collect(Collectors.toList());

        List<OrderItem> allItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));

        Map<Long, List<OrderItem>> itemMap = allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        orders.forEach(order -> {
            List<OrderItem> items = itemMap.getOrDefault(order.getOrderId(), List.of());
            List<OrderItemVo> itemVos = items.stream().map(item -> {
                OrderItemVo iv = new OrderItemVo();
                iv.setBookId(item.getBookId().intValue());
                iv.setOrderItemId(item.getId());
                iv.setBookTitle(item.getBookTitle());
                iv.setCoverUrl(item.getCoverImageUrl());
                iv.setQuantity(item.getQuantity());
                iv.setPrice(item.getPrice());
                iv.setDiscount(BigDecimal.ZERO);
                return iv;
            }).collect(Collectors.toList());
            order.setItems(itemVos);
        });

        return orders;
    }

    @Override
    @Transactional
    public void adminShipOrder(OrderOperateDto dto) {
        // 鉴权
        User user = userMapper.selectById(dto.getUserId());

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(),
                    ErrorCode.USER_NOT_FOUND.getMessage());
        }

        if (user.getRole()!= 2){
            throw new BusinessException(ErrorCode.ORDER_NO_PERMISSION.getCode(),
                    ErrorCode.ORDER_NO_PERMISSION.getMessage());
        }

        Order order = orderMapper.selectByOrderNo(dto.getOrderNo());
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND.getCode(),
                    ErrorCode.ORDER_NOT_FOUND.getMessage());
        }

        if (!Objects.equals(order.getOrderStatus(), StatusEnum.OrderStatus.PENDING_SHIP.getCode())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_FLOW_ERROR.getCode(),
                    "当前订单状态不允许发货，仅待发货状态的订单可以发货");
        }

        order.setOrderStatus(StatusEnum.OrderStatus.SHIPPED.getCode());
        order.setUpdateTime(LocalDateTime.now());
        order.setDeliveryTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 发送发货通知给用户
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, order.getOrderId());
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
        List<String> bookNames = items.stream()
                .map(OrderItem::getBookTitle)
                .collect(Collectors.toList());
        notificationService.notifyUserOrderShipped(order.getOrderNo(), order.getUserId(), bookNames);
    }

    @Override
    public IPage<AdminOrderListVo> adminListPaidOrders(Long adminId, Integer page, Integer pageSize) {
        validateAdmin(adminId);

        Page<Order> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getPayStatus, StatusEnum.PayStatus.PAID.getCode())
                .orderByDesc(Order::getCreateTime);

        IPage<Order> orderPage = orderMapper.selectPage(pageParam, wrapper);

        return convertToAdminOrderListVoPage(orderPage, page, pageSize);
    }

    @Override
    public List<AdminOrderListVo> adminListPaidOrdersAll(Long adminId) {
        validateAdmin(adminId);

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getPayStatus, StatusEnum.PayStatus.PAID.getCode())
                .orderByDesc(Order::getCreateTime);

        List<Order> orders = orderMapper.selectList(wrapper);
        return convertToAdminOrderListVoList(orders);
    }

    @Override
    public AdminOrderDetailVo adminGetOrderDetail(Long adminId, String orderNo) {
        validateAdmin(adminId);

        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND.getCode(),
                    ErrorCode.ORDER_NOT_FOUND.getMessage());
        }

        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, order.getOrderId());
        List<OrderItem> items = orderItemMapper.selectList(wrapper);

        return toAdminOrderDetailVo(order, items);
    }

    @Override
    public IPage<AdminOrderListVo> adminListAllOrders(Long adminId, Integer page, Integer pageSize) {
        validateAdmin(adminId);

        Page<Order> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Order::getCreateTime);

        IPage<Order> orderPage = orderMapper.selectPage(pageParam, wrapper);

        return convertToAdminOrderListVoPage(orderPage, page, pageSize);
    }

    @Override
    public List<AdminOrderListVo> adminListAllOrdersAll(Long adminId) {
        validateAdmin(adminId);

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Order::getCreateTime);

        List<Order> orders = orderMapper.selectList(wrapper);
        return convertToAdminOrderListVoList(orders);
    }

    @Override
    public IPage<AdminOrderListVo> adminListOrdersByStatus(Long adminId, Integer orderStatus, Integer page, Integer pageSize) {
        validateAdmin(adminId);

        Page<Order> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderStatus, orderStatus)
                .orderByDesc(Order::getCreateTime);

        IPage<Order> orderPage = orderMapper.selectPage(pageParam, wrapper);

        return convertToAdminOrderListVoPage(orderPage, page, pageSize);
    }

    @Override
    public List<AdminOrderListVo> adminListOrdersByStatusAll(Long adminId, Integer orderStatus) {
        validateAdmin(adminId);

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderStatus, orderStatus)
                .orderByDesc(Order::getCreateTime);

        List<Order> orders = orderMapper.selectList(wrapper);
        return convertToAdminOrderListVoList(orders);
    }

    private void validateAdmin(Long adminId) {
        User user = userMapper.selectById(adminId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(),
                    ErrorCode.USER_NOT_FOUND.getMessage());
        }
        if (user.getRole() != 2) {
            throw new BusinessException(ErrorCode.ORDER_NO_PERMISSION.getCode(),
                    "无权限访问，仅管理员可访问");
        }
    }

    private IPage<AdminOrderListVo> convertToAdminOrderListVoPage(IPage<Order> orderPage, Integer page, Integer pageSize) {
        List<Order> orders = orderPage.getRecords();
        if (orders.isEmpty()) {
            Page<AdminOrderListVo> emptyPage = new Page<>(page, pageSize);
            emptyPage.setTotal(0);
            return emptyPage;
        }

        List<Long> orderIds = orders.stream()
                .map(Order::getOrderId)
                .collect(Collectors.toList());

        List<OrderItem> allItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));

        Map<Long, List<OrderItem>> itemMap = allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        List<AdminOrderListVo> voList = orders.stream()
                .map(order -> toAdminOrderListVo(order, itemMap.getOrDefault(order.getOrderId(), List.of())))
                .collect(Collectors.toList());

        Page<AdminOrderListVo> voPage = new Page<>(page, pageSize);
        voPage.setTotal(orderPage.getTotal());
        voPage.setRecords(voList);

        return voPage;
    }

    private List<AdminOrderListVo> convertToAdminOrderListVoList(List<Order> orders) {
        if (orders.isEmpty()) {
            return List.of();
        }

        List<Long> orderIds = orders.stream()
                .map(Order::getOrderId)
                .collect(Collectors.toList());

        List<OrderItem> allItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));

        Map<Long, List<OrderItem>> itemMap = allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        return orders.stream()
                .map(order -> toAdminOrderListVo(order, itemMap.getOrDefault(order.getOrderId(), List.of())))
                .collect(Collectors.toList());
    }

    private AdminOrderListVo toAdminOrderListVo(Order order, List<OrderItem> items) {
        AdminOrderListVo vo = new AdminOrderListVo();
        vo.setOrderId(order.getOrderId());
        vo.setOrderNo(order.getOrderNo());
        vo.setOrderStatus(order.getOrderStatus());
        vo.setPayStatus(order.getPayStatus());
        vo.setTotalPrice(order.getTotalAmount());
        vo.setDiscountPrice(order.getDiscountAmount());
        vo.setPayPrice(order.getPayAmount());
        vo.setCreateTime(order.getCreateTime());
        vo.setUserId(order.getUserId());
        vo.setUsername(order.getUsername());
        vo.setConsignee(order.getConsignee());
        vo.setPhone(order.getPhone());

        List<OrderItemVo> itemVos = items.stream().map(item -> {
            OrderItemVo iv = new OrderItemVo();
            iv.setBookId(item.getBookId().intValue());
            iv.setOrderItemId(item.getId());
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

    private AdminOrderDetailVo toAdminOrderDetailVo(Order order, List<OrderItem> items) {
        AdminOrderDetailVo vo = new AdminOrderDetailVo();
        vo.setOrderId(order.getOrderId());
        vo.setOrderNo(order.getOrderNo());
        vo.setOrderStatus(order.getOrderStatus());
        vo.setPayStatus(order.getPayStatus());
        vo.setTotalPrice(order.getTotalAmount());
        vo.setDiscountPrice(order.getDiscountAmount());
        vo.setPayPrice(order.getPayAmount());
        vo.setCreateTime(order.getCreateTime());
        vo.setPayTime(order.getPayTime());
        vo.setShipTime(order.getDeliveryTime());
        vo.setConsignee(order.getConsignee());
        vo.setPhone(order.getPhone());
        vo.setAddress(order.getAddress());
        vo.setRemark(order.getRemark());
        vo.setPayType(order.getPayType());
        vo.setUserId(order.getUserId());
        vo.setUsername(order.getUsername());

        List<OrderItemVo> itemVos = items.stream().map(item -> {
            OrderItemVo iv = new OrderItemVo();
            iv.setOrderItemId(item.getId());
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

    private void validateQueryParams(OrderQueryDto query) {
        if (query.getBeginTime() != null && query.getEndTime() != null) {
            if (query.getBeginTime().isAfter(query.getEndTime())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "开始时间不能晚于结束时间");
            }
        }
        if (query.getOrderStatus() != null && (query.getOrderStatus() < 1 || query.getOrderStatus() > 8)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "订单状态不合法");
        }
    }
}

