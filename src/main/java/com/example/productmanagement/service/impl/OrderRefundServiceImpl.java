package com.example.productmanagement.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.productmanagement.common.ErrorCode;
import com.example.productmanagement.common.StatusEnum;
import com.example.productmanagement.dto.AdminAuditRefundDto;
import com.example.productmanagement.dto.AdminProcessRefundDto;
import com.example.productmanagement.dto.ApplyRefundDto;
import com.example.productmanagement.entity.*;
import com.example.productmanagement.exception.BusinessException;
import com.example.productmanagement.mapper.*;
import com.example.productmanagement.service.NotificationService;
import com.example.productmanagement.service.OrderRefundService;
import com.example.productmanagement.vo.AdminRefundListVo;
import com.example.productmanagement.vo.RefundDetailVo;
import com.example.productmanagement.vo.RefundHistoryVo;
import com.example.productmanagement.vo.RefundListVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class OrderRefundServiceImpl implements OrderRefundService {

    private final OrderRefundMapper orderRefundMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;
    private final BookInfoMapper bookInfoMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    public OrderRefundServiceImpl(OrderRefundMapper orderRefundMapper, OrderItemMapper orderItemMapper, OrderMapper orderMapper, BookInfoMapper bookInfoMapper, UserMapper userMapper, NotificationService notificationService) {
        this.orderRefundMapper = orderRefundMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderMapper = orderMapper;
        this.bookInfoMapper = bookInfoMapper;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public void applyRefund(ApplyRefundDto applyRefundDto) {
        Long itemId = applyRefundDto.getOrderItemId();
        OrderItem item = orderItemMapper.selectById(itemId);
        // 检查商品是否存在
        if (item == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND.getCode(),
                    ErrorCode.ORDER_NOT_FOUND.getMessage());
        }

        // 检查订单是否存在
        Order order = orderMapper.selectById(item.getOrderId());
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND.getCode(),
                    ErrorCode.ORDER_NOT_FOUND.getMessage());
        }

        // 订单鉴权
        if (!Objects.equals(applyRefundDto.getUserId(), order.getUserId())) {
            throw new BusinessException(ErrorCode.ORDER_NO_PERMISSION.getCode(),
                    ErrorCode.ORDER_NO_PERMISSION.getMessage());
        }

        // 检查退款类型是否合法
        if (!StatusEnum.RefundType.validateStatus(applyRefundDto.getRefundType())){
            throw new BusinessException(ErrorCode.REFUND_TYPE_ERROR.getCode(),
                    ErrorCode.REFUND_TYPE_ERROR.getMessage());
        }

        // 检查退款请求是否已经存在
        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderRefund::getOrderItemId, itemId);
        wrapper.in(OrderRefund::getRefundStatus,
                StatusEnum.RefundStatus.PENDING_REVIEW.getCode(),
                StatusEnum.RefundStatus.REVIEW_APPROVED.getCode(),
                StatusEnum.RefundStatus.SHIPPED.getCode(),
                StatusEnum.RefundStatus.RECEIVED.getCode());
        OrderRefund existingRefund = orderRefundMapper.selectOne(wrapper);
        if (existingRefund != null) {
            throw new BusinessException(ErrorCode.REFUND_STATUS_ERROR.getCode(),
                    "该商品的退款请求正在审核中");
        }

        OrderRefund refund = new OrderRefund();
        refund.setRefundNo(IdWorker.getIdStr());
        refund.setOrderItemId(itemId);
        refund.setUserId(applyRefundDto.getUserId());
        refund.setRefundType(applyRefundDto.getRefundType());
        refund.setRefundReason(applyRefundDto.getRefundReason());
        refund.setRefundDesc(applyRefundDto.getDescription());
        refund.setRefundAmount(item.getPayAmount());
        refund.setRefundStatus(StatusEnum.RefundStatus.PENDING_REVIEW.getCode());
        refund.setApplyTime(LocalDateTime.now());

        orderRefundMapper.insert(refund);

        order.setOrderStatus(StatusEnum.OrderStatus.AFTER_SALE.getCode());
        orderMapper.updateById(order);

        // 通知卖家处理
        notificationService.notifySellerRefundApply(refund.getRefundNo(), applyRefundDto.getUserId(), item.getPayAmount());
    }

    @Override
    @Transactional
    public void auditRefund(AdminAuditRefundDto auditRefundDto) {
        OrderRefund refund = orderRefundMapper.selectById(auditRefundDto.getRefundId());
        if (refund == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "退款申请不存在");
        }

        if (!Objects.equals(refund.getRefundStatus(), StatusEnum.RefundStatus.PENDING_REVIEW.getCode())) {
            throw new BusinessException(ErrorCode.REFUND_STATUS_ERROR.getCode(),
                    "当前退款状态不允许审核");
        }

        refund.setAuditTime(LocalDateTime.now());
        refund.setProcessAdmin(auditRefundDto.getAdminId());

        // 同意请求，更新退款单状态为已同意
        if (auditRefundDto.getApproved()) {
            refund.setRefundStatus(StatusEnum.RefundStatus.REVIEW_APPROVED.getCode());
        } else {
            // 拒绝请求
            refund.setRefundStatus(StatusEnum.RefundStatus.REVIEW_REJECTED.getCode());
            String reason = auditRefundDto.getRejectReason();
            // 判断原因是否为空
            if (reason == null || reason.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(),
                        ErrorCode.PARAM_ERROR.getMessage());
            }
            refund.setRejectReason(reason);

            // 更新订单状态为已完成
            OrderItem item = orderItemMapper.selectById(refund.getOrderItemId());
            if (item != null) {
                Order order = orderMapper.selectById(item.getOrderId());
                if (order != null) {
                    boolean hasOtherRefund = checkOtherPendingRefund(order.getOrderId(), refund.getId());
                    if (!hasOtherRefund) {
                        order.setOrderStatus(StatusEnum.OrderStatus.COMPLETED.getCode());
                        orderMapper.updateById(order);
                    }
                }
            }
        }

        orderRefundMapper.updateById(refund);

        // 通知用户审核结果
        OrderItem item = orderItemMapper.selectById(refund.getOrderItemId());
        BigDecimal refundAmount = (item != null) ? item.getTotalPrice() : BigDecimal.ZERO;
        notificationService.notifyUserRefundResult(refund.getRefundNo(), refund.getUserId(), auditRefundDto.getApproved(), refundAmount);
    }

    @Override
    @Transactional
    public void processRefund(AdminProcessRefundDto processRefundDto) {
        OrderRefund refund = orderRefundMapper.selectById(processRefundDto.getRefundId());
        if (refund == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "退款申请不存在");
        }

        if (!Objects.equals(refund.getRefundStatus(), StatusEnum.RefundStatus.REVIEW_APPROVED.getCode()) &&
                !Objects.equals(refund.getRefundStatus(), StatusEnum.RefundStatus.RECEIVED.getCode())) {
            throw new BusinessException(ErrorCode.REFUND_STATUS_ERROR.getCode(),
                    "当前退款状态不允许执行退款");
        }

        OrderItem item = orderItemMapper.selectById(refund.getOrderItemId());
        if (item == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND.getCode(), "订单商品不存在");
        }

        // 获取退款金额
        BigDecimal refundAmount = item.getPayAmount();

        refund.setRefundAmount(refundAmount);
        refund.setRefundStatus(StatusEnum.RefundStatus.REFUNDED.getCode());
        refund.setRefundFinishTime(LocalDateTime.now());

        orderRefundMapper.updateById(refund);

        Order order = orderMapper.selectById(item.getOrderId());
        if (order != null) {
            boolean hasOtherRefund = checkOtherPendingRefund(order.getOrderId(), refund.getId());
            if (!hasOtherRefund) {
                order.setOrderStatus(StatusEnum.OrderStatus.REFUND.getCode());
                orderMapper.updateById(order);
            }
        }

        notificationService.notifyUserRefund(refund.getRefundNo(), refund.getUserId(), refundAmount);
    }

    private boolean checkOtherPendingRefund(Long orderId, Long excludeRefundId) {
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orderId);
        java.util.List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

        if (items.isEmpty()) {
            return false;
        }

        java.util.List<Long> itemIds = items.stream()
                .map(OrderItem::getId)
                .collect(java.util.stream.Collectors.toList());

        LambdaQueryWrapper<OrderRefund> refundWrapper = new LambdaQueryWrapper<>();
        refundWrapper.in(OrderRefund::getOrderItemId, itemIds)
                .ne(OrderRefund::getId, excludeRefundId)
                .in(OrderRefund::getRefundStatus,
                        StatusEnum.RefundStatus.PENDING_REVIEW.getCode(),
                        StatusEnum.RefundStatus.REVIEW_APPROVED.getCode(),
                        StatusEnum.RefundStatus.SHIPPED.getCode(),
                        StatusEnum.RefundStatus.RECEIVED.getCode());

        return orderRefundMapper.selectCount(refundWrapper) > 0;
    }

    @Override
    public IPage<RefundListVo> listRefunds(Long userId, String refundNo, Integer refundType, Integer refundStatus,
                                           LocalDateTime beginTime, LocalDateTime endTime, Integer page, Integer pageSize) {
        Page<OrderRefund> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<OrderRefund> wrapper = buildQueryWrapper(userId, refundNo, refundStatus, refundType, beginTime, endTime);
        IPage<OrderRefund> refundPage = orderRefundMapper.selectPage(pageParam, wrapper);
        return refundPage.convert(this::convertToVo);
    }

    @Override
    public List<RefundListVo> listAllRefunds(Long userId) {
        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderRefund::getUserId, userId)
                .orderByDesc(OrderRefund::getApplyTime);
        List<OrderRefund> refunds = orderRefundMapper.selectList(wrapper);
        return refunds.stream().map(this::convertToVo).collect(Collectors.toList());
    }

    @Override
    public IPage<RefundListVo> listRefundsByStatus(Long userId, Integer refundStatus, Integer page, Integer pageSize) {
        Page<OrderRefund> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderRefund::getUserId, userId)
                .eq(OrderRefund::getRefundStatus, refundStatus)
                .orderByDesc(OrderRefund::getApplyTime);
        IPage<OrderRefund> refundPage = orderRefundMapper.selectPage(pageParam, wrapper);
        return refundPage.convert(this::convertToVo);
    }

    @Override
    public List<RefundListVo> listAllRefundsByStatus(Long userId, Integer refundStatus) {
        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderRefund::getUserId, userId)
                .eq(OrderRefund::getRefundStatus, refundStatus)
                .orderByDesc(OrderRefund::getApplyTime);
        List<OrderRefund> refunds = orderRefundMapper.selectList(wrapper);
        return refunds.stream().map(this::convertToVo).collect(Collectors.toList());
    }

    @Override
    public IPage<RefundListVo> listRefundsByType(Long userId, Integer refundType, Integer page, Integer pageSize) {
        Page<OrderRefund> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderRefund::getUserId, userId)
                .eq(OrderRefund::getRefundType, refundType)
                .orderByDesc(OrderRefund::getApplyTime);
        IPage<OrderRefund> refundPage = orderRefundMapper.selectPage(pageParam, wrapper);
        return refundPage.convert(this::convertToVo);
    }

    @Override
    public List<RefundListVo> listAllRefundsByType(Long userId, Integer refundType) {
        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderRefund::getUserId, userId)
                .eq(OrderRefund::getRefundType, refundType)
                .orderByDesc(OrderRefund::getApplyTime);
        List<OrderRefund> refunds = orderRefundMapper.selectList(wrapper);
        return refunds.stream().map(this::convertToVo).collect(Collectors.toList());
    }

    @Override
    public IPage<RefundListVo> listRefundsByTimeRange(Long userId, LocalDateTime beginTime, LocalDateTime endTime, Integer page, Integer pageSize) {
        Page<OrderRefund> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderRefund::getUserId, userId);
        if (beginTime != null) {
            wrapper.ge(OrderRefund::getApplyTime, beginTime);
        }
        if (endTime != null) {
            wrapper.le(OrderRefund::getApplyTime, endTime);
        }
        wrapper.orderByDesc(OrderRefund::getApplyTime);
        IPage<OrderRefund> refundPage = orderRefundMapper.selectPage(pageParam, wrapper);
        return refundPage.convert(this::convertToVo);
    }

    @Override
    public List<RefundListVo> listAllRefundsByTimeRange(Long userId, LocalDateTime beginTime, LocalDateTime endTime) {
        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderRefund::getUserId, userId);
        if (beginTime != null) {
            wrapper.ge(OrderRefund::getApplyTime, beginTime);
        }
        if (endTime != null) {
            wrapper.le(OrderRefund::getApplyTime, endTime);
        }
        wrapper.orderByDesc(OrderRefund::getApplyTime);
        List<OrderRefund> refunds = orderRefundMapper.selectList(wrapper);
        return refunds.stream().map(this::convertToVo).collect(Collectors.toList());
    }

    @Override
    public IPage<RefundListVo> queryRefunds(Long userId, Integer refundStatus, Integer refundType, LocalDateTime beginTime, LocalDateTime endTime, Integer page, Integer pageSize) {
        Page<OrderRefund> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<OrderRefund> wrapper = buildQueryWrapper(userId, null, refundStatus, refundType, beginTime, endTime);
        IPage<OrderRefund> refundPage = orderRefundMapper.selectPage(pageParam, wrapper);
        return refundPage.convert(this::convertToVo);
    }

    @Override
    public List<RefundListVo> queryAllRefunds(Long userId, Integer refundStatus, Integer refundType, LocalDateTime beginTime, LocalDateTime endTime) {
        LambdaQueryWrapper<OrderRefund> wrapper = buildQueryWrapper(userId, null, refundStatus, refundType, beginTime, endTime);
        List<OrderRefund> refunds = orderRefundMapper.selectList(wrapper);
        return refunds.stream().map(this::convertToVo).collect(Collectors.toList());
    }

    private LambdaQueryWrapper<OrderRefund> buildQueryWrapper(Long userId, String refundNo, Integer refundStatus, Integer refundType,
                                                              LocalDateTime beginTime, LocalDateTime endTime) {
        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderRefund::getUserId, userId);
        if (refundNo != null && !refundNo.trim().isEmpty()) {
            wrapper.like(OrderRefund::getRefundNo, refundNo.trim());
        }
        if (refundStatus != null) {
            wrapper.eq(OrderRefund::getRefundStatus, refundStatus);
        }
        if (refundType != null) {
            wrapper.eq(OrderRefund::getRefundType, refundType);
        }
        if (beginTime != null) {
            wrapper.ge(OrderRefund::getApplyTime, beginTime);
        }
        if (endTime != null) {
            wrapper.le(OrderRefund::getApplyTime, endTime);
        }
        wrapper.orderByDesc(OrderRefund::getApplyTime);
        return wrapper;
    }

    private RefundListVo convertToVo(OrderRefund refund) {
        RefundListVo vo = new RefundListVo();
        vo.setId(refund.getId());
        vo.setRefundNo(refund.getRefundNo());
        vo.setRefundStatus(refund.getRefundStatus());
        vo.setRefundType(refund.getRefundType());
        vo.setRefundAmount(refund.getRefundAmount() != null ? refund.getRefundAmount().toString() : "0.00");
        vo.setCreateTime(refund.getApplyTime());

        // 获取订单项和书籍信息
        OrderItem orderItem = orderItemMapper.selectById(refund.getOrderItemId());
        if (orderItem != null) {
            Order order = orderMapper.selectById(orderItem.getOrderId());
            if (order != null) {
                vo.setOrderNo(order.getOrderNo());
            }
            BookInfo bookInfo = bookInfoMapper.selectById(orderItem.getBookId());
            if (bookInfo != null) {
                vo.setBookName(bookInfo.getTitle());
                vo.setCoverUrl(bookInfo.getCoverImageUrl());
            }
        }

        return vo;
    }

    @Override
    public RefundDetailVo getRefundDetail(Long userId, String refundNo) {
        // 查询退款单
        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderRefund::getRefundNo, refundNo);
        OrderRefund refund = orderRefundMapper.selectOne(wrapper);

        if (refund == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "退款单不存在");
        }

        // 权限校验：只能查看自己的退款单
        if (!Objects.equals(userId, refund.getUserId())) {
            throw new BusinessException(ErrorCode.ORDER_NO_PERMISSION.getCode(),
                    ErrorCode.ORDER_NO_PERMISSION.getMessage());
        }

        return buildRefundDetailVo(refund);
    }

    @Override
    public RefundDetailVo adminGetRefundDetail(Long adminId, String refundNo) {
        // 校验管理员权限
        validateAdmin(adminId);

        // 查询退款单
        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderRefund::getRefundNo, refundNo);
        OrderRefund refund = orderRefundMapper.selectOne(wrapper);

        if (refund == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "退款单不存在");
        }

        return buildRefundDetailVo(refund);
    }

    @Override
    public IPage<AdminRefundListVo> adminListRefunds(Long adminId, Integer page, Integer pageSize) {
        validateAdmin(adminId);

        Page<OrderRefund> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(OrderRefund::getApplyTime);
        IPage<OrderRefund> refundPage = orderRefundMapper.selectPage(pageParam, wrapper);

        return refundPage.convert(this::convertToAdminVo);
    }

    @Override
    public List<AdminRefundListVo> adminListAllRefunds(Long adminId) {
        validateAdmin(adminId);

        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(OrderRefund::getApplyTime);
        List<OrderRefund> refunds = orderRefundMapper.selectList(wrapper);
        return refunds.stream().map(this::convertToAdminVo).collect(Collectors.toList());
    }

    @Override
    public AdminRefundListVo adminGetRefundByNo(Long adminId, String refundNo) {
        validateAdmin(adminId);

        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderRefund::getRefundNo, refundNo);
        OrderRefund refund = orderRefundMapper.selectOne(wrapper);

        if (refund == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "退款单不存在");
        }

        return convertToAdminVo(refund);
    }

    @Override
    public IPage<AdminRefundListVo> adminListRefundsByType(Long adminId, Integer refundType, Integer page, Integer pageSize) {
        validateAdmin(adminId);

        Page<OrderRefund> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderRefund::getRefundType, refundType)
                .orderByDesc(OrderRefund::getApplyTime);
        IPage<OrderRefund> refundPage = orderRefundMapper.selectPage(pageParam, wrapper);

        return refundPage.convert(this::convertToAdminVo);
    }

    @Override
    public List<AdminRefundListVo> adminListAllRefundsByType(Long adminId, Integer refundType) {
        validateAdmin(adminId);

        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderRefund::getRefundType, refundType)
                .orderByDesc(OrderRefund::getApplyTime);
        List<OrderRefund> refunds = orderRefundMapper.selectList(wrapper);
        return refunds.stream().map(this::convertToAdminVo).collect(Collectors.toList());
    }

    @Override
    public IPage<AdminRefundListVo> adminListRefundsByStatus(Long adminId, Integer refundStatus, Integer page, Integer pageSize) {
        validateAdmin(adminId);

        Page<OrderRefund> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderRefund::getRefundStatus, refundStatus)
                .orderByDesc(OrderRefund::getApplyTime);
        IPage<OrderRefund> refundPage = orderRefundMapper.selectPage(pageParam, wrapper);

        return refundPage.convert(this::convertToAdminVo);
    }

    @Override
    public List<AdminRefundListVo> adminListAllRefundsByStatus(Long adminId, Integer refundStatus) {
        validateAdmin(adminId);

        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderRefund::getRefundStatus, refundStatus)
                .orderByDesc(OrderRefund::getApplyTime);
        List<OrderRefund> refunds = orderRefundMapper.selectList(wrapper);
        return refunds.stream().map(this::convertToAdminVo).collect(Collectors.toList());
    }

    @Override
    public IPage<AdminRefundListVo> adminListRefundsByTimeRange(Long adminId, LocalDateTime beginTime, LocalDateTime endTime, Integer page, Integer pageSize) {
        validateAdmin(adminId);

        Page<OrderRefund> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
        if (beginTime != null) {
            wrapper.ge(OrderRefund::getApplyTime, beginTime);
        }
        if (endTime != null) {
            wrapper.le(OrderRefund::getApplyTime, endTime);
        }
        wrapper.orderByDesc(OrderRefund::getApplyTime);
        IPage<OrderRefund> refundPage = orderRefundMapper.selectPage(pageParam, wrapper);

        return refundPage.convert(this::convertToAdminVo);
    }

    @Override
    public List<AdminRefundListVo> adminListAllRefundsByTimeRange(Long adminId, LocalDateTime beginTime, LocalDateTime endTime) {
        validateAdmin(adminId);

        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
        if (beginTime != null) {
            wrapper.ge(OrderRefund::getApplyTime, beginTime);
        }
        if (endTime != null) {
            wrapper.le(OrderRefund::getApplyTime, endTime);
        }
        wrapper.orderByDesc(OrderRefund::getApplyTime);
        List<OrderRefund> refunds = orderRefundMapper.selectList(wrapper);
        return refunds.stream().map(this::convertToAdminVo).collect(Collectors.toList());
    }

    @Override
    public IPage<AdminRefundListVo> adminQueryRefunds(Long adminId, String refundNo, Integer refundStatus, Integer refundType,
                                                      LocalDateTime beginTime, LocalDateTime endTime, Integer page, Integer pageSize) {
        validateAdmin(adminId);

        Page<OrderRefund> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<OrderRefund> wrapper = buildAdminQueryWrapper(refundNo, refundStatus, refundType, beginTime, endTime);
        IPage<OrderRefund> refundPage = orderRefundMapper.selectPage(pageParam, wrapper);

        return refundPage.convert(this::convertToAdminVo);
    }

    @Override
    public List<AdminRefundListVo> adminQueryAllRefunds(Long adminId, String refundNo, Integer refundStatus, Integer refundType,
                                                        LocalDateTime beginTime, LocalDateTime endTime) {
        validateAdmin(adminId);

        LambdaQueryWrapper<OrderRefund> wrapper = buildAdminQueryWrapper(refundNo, refundStatus, refundType, beginTime, endTime);
        List<OrderRefund> refunds = orderRefundMapper.selectList(wrapper);
        return refunds.stream().map(this::convertToAdminVo).collect(Collectors.toList());
    }

    private LambdaQueryWrapper<OrderRefund> buildAdminQueryWrapper(String refundNo, Integer refundStatus, Integer refundType,
                                                                   LocalDateTime beginTime, LocalDateTime endTime) {
        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();

        if (refundNo != null && !refundNo.isEmpty()) {
            wrapper.like(OrderRefund::getRefundNo, refundNo);
        }
        if (refundStatus != null) {
            wrapper.eq(OrderRefund::getRefundStatus, refundStatus);
        }
        if (refundType != null) {
            wrapper.eq(OrderRefund::getRefundType, refundType);
        }
        if (beginTime != null) {
            wrapper.ge(OrderRefund::getApplyTime, beginTime);
        }
        if (endTime != null) {
            wrapper.le(OrderRefund::getApplyTime, endTime);
        }
        wrapper.orderByDesc(OrderRefund::getApplyTime);
        return wrapper;
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

    private AdminRefundListVo convertToAdminVo(OrderRefund refund) {
        AdminRefundListVo vo = new AdminRefundListVo();
        vo.setId(refund.getId());
        vo.setRefundNo(refund.getRefundNo());
        vo.setRefundStatus(refund.getRefundStatus());
        vo.setRefundStatusDesc(getRefundStatusDesc(refund.getRefundStatus()));
        vo.setRefundType(refund.getRefundType());
        vo.setRefundTypeDesc(getRefundTypeDesc(refund.getRefundType()));
        vo.setRefundAmount(refund.getRefundAmount() != null ? refund.getRefundAmount().toString() : "0.00");
        vo.setApplyTime(refund.getApplyTime());
        vo.setAuditTime(refund.getAuditTime());
        vo.setRefundFinishTime(refund.getRefundFinishTime());
        vo.setCreateTime(refund.getCreateTime());

        // 用户信息
        vo.setUserId(refund.getUserId());
        User user = userMapper.selectById(refund.getUserId());
        if (user != null) {
            vo.setUsername(user.getLoginAccount());
        }

        // 处理管理员信息
        vo.setProcessAdminId(refund.getProcessAdmin());
        if (refund.getProcessAdmin() != null) {
            vo.setProcessAdminName(getUserName(refund.getProcessAdmin()));
        }

        // 获取订单项和书籍信息
        OrderItem orderItem = orderItemMapper.selectById(refund.getOrderItemId());
        if (orderItem != null) {
            Order order = orderMapper.selectById(orderItem.getOrderId());
            if (order != null) {
                vo.setOrderNo(order.getOrderNo());
            }
            BookInfo bookInfo = bookInfoMapper.selectById(orderItem.getBookId());
            if (bookInfo != null) {
                vo.setBookName(bookInfo.getTitle());
                vo.setCoverUrl(bookInfo.getCoverImageUrl());
            }
        }

        return vo;
    }

    private RefundDetailVo buildRefundDetailVo(OrderRefund refund) {
        RefundDetailVo vo = new RefundDetailVo();

        // 1. 退款单基础信息
        vo.setId(refund.getId());
        vo.setRefundNo(refund.getRefundNo());
        vo.setRefundType(refund.getRefundType());
        vo.setRefundTypeDesc(getRefundTypeDesc(refund.getRefundType()));
        vo.setRefundStatus(refund.getRefundStatus());
        vo.setRefundStatusDesc(getRefundStatusDesc(refund.getRefundStatus()));

        // 2. 退款金额信息
        vo.setRefundAmount(refund.getRefundAmount());

        // 3. 退款原因与说明
        vo.setRefundReason(refund.getRefundReason());
        vo.setRefundDesc(refund.getRefundDesc());
        vo.setRejectReason(refund.getRejectReason());

        // 4. 时间信息
        vo.setApplyTime(refund.getApplyTime());
        vo.setAuditTime(refund.getAuditTime());
        vo.setReturnTime(refund.getReturnTime());
        vo.setReceiveTime(refund.getReceiveTime());
        vo.setRefundFinishTime(refund.getRefundFinishTime());
        vo.setCreateTime(refund.getCreateTime());

        // 5. 用户信息
        vo.setUserId(refund.getUserId());
        User user = userMapper.selectById(refund.getUserId());
        if (user != null) {
            vo.setUsername(user.getLoginAccount());
        }

        // 6. 管理员信息
        vo.setProcessAdminId(refund.getProcessAdmin());
        if (refund.getProcessAdmin() != null) {
            User admin = userMapper.selectById(refund.getProcessAdmin());
            if (admin != null) {
                vo.setProcessAdminName(admin.getLoginAccount());
            }
        }

        // 7. 获取订单项和订单信息
        OrderItem orderItem = orderItemMapper.selectById(refund.getOrderItemId());
        if (orderItem != null) {
            vo.setOrderItemId(orderItem.getId());
            vo.setBookId(orderItem.getBookId());
            vo.setQuantity(orderItem.getQuantity());
            vo.setPrice(orderItem.getPrice());
            vo.setItemTotalPrice(orderItem.getTotalPrice());

            // 设置退款金额（如果数据库中没有，使用订单项的支付金额）
            if (vo.getRefundAmount() == null) {
                vo.setRefundAmount(orderItem.getPayAmount());
            }

            // 获取书籍信息
            BookInfo bookInfo = bookInfoMapper.selectById(orderItem.getBookId());
            if (bookInfo != null) {
                vo.setBookName(bookInfo.getTitle());
                vo.setCoverUrl(bookInfo.getCoverImageUrl());
            }

            // 获取订单信息
            Order order = orderMapper.selectById(orderItem.getOrderId());
            if (order != null) {
                vo.setOrderId(order.getOrderId());
                vo.setOrderNo(order.getOrderNo());
                vo.setOrderPayTime(order.getPayTime());
                vo.setOrderPayAmount(order.getPayAmount());
                vo.setFreightAmount(order.getFreightAmount());
            }
        }

        // 8. 计算当前步骤（用于前端进度条展示）
        vo.setCurrentStep(calculateCurrentStep(refund.getRefundStatus()));

        // 9. 构建协商历史
        vo.setHistoryList(buildRefundHistory(refund));

        return vo;
    }

    private String getRefundTypeDesc(Integer refundType) {
        if (refundType == null) return "未知";
        return refundType == 0 ? "仅退款" : "退货退款";
    }

    private String getRefundStatusDesc(Integer refundStatus) {
        if (refundStatus == null) return "未知";
        switch (refundStatus) {
            case 0: return "待审核";
            case 1: return "审核通过";
            case 2: return "用户已寄回";
            case 3: return "商家已收货";
            case 4: return "已退款";
            case 5: return "审核拒绝";
            case 6: return "用户已取消";
            default: return "未知状态";
        }
    }

    private Integer calculateCurrentStep(Integer refundStatus) {
        if (refundStatus == null) return 1;
        // 1-买家申请退款，2-卖家处理，3-退款完毕
        switch (refundStatus) {
            case 0: // 待审核
                return 1;
            case 1: // 审核通过
            case 2: // 用户已寄回
            case 3: // 商家已收货
                return 2;
            case 4: // 已退款
            case 5: // 审核拒绝
            case 6: // 用户已取消
                return 3;
            default:
                return 1;
        }
    }

    private List<RefundHistoryVo> buildRefundHistory(OrderRefund refund) {
        List<RefundHistoryVo> historyList = new ArrayList<>();

        // 1. 用户申请退款
        if (refund.getApplyTime() != null) {
            RefundHistoryVo history = new RefundHistoryVo();
            history.setId(1L);
            history.setOperatorRole(1);
            history.setOperatorRoleDesc("买家");
            history.setOperatorName(getUserName(refund.getUserId()));
            history.setOperatorAvatar("");
            history.setActionType("申请退款");
            history.setContent("发起了退款申请");
            history.setRefundAmount(refund.getRefundAmount() != null ? refund.getRefundAmount().toString() : null);
            history.setRefundReason(refund.getRefundReason());
            history.setOperateTime(refund.getApplyTime());
            historyList.add(history);
        }

        // 2. 卖家审核
        if (refund.getAuditTime() != null) {
            RefundHistoryVo history = new RefundHistoryVo();
            history.setId(2L);
            history.setOperatorRole(2);
            history.setOperatorRoleDesc("卖家");
            history.setOperatorName(getUserName(refund.getProcessAdmin()));
            history.setOperatorAvatar("");

            if (refund.getRefundStatus() == 5) { // 审核拒绝
                history.setActionType("审核拒绝");
                history.setContent("拒绝了退款申请");
                history.setRejectReason(refund.getRejectReason());
            } else {
                history.setActionType("审核通过");
                history.setContent("同意了退款申请");
            }
            history.setOperateTime(refund.getAuditTime());
            historyList.add(history);
        }

        // 3. 退款完成
        if (refund.getRefundFinishTime() != null && refund.getRefundStatus() == 4) {
            RefundHistoryVo history = new RefundHistoryVo();
            history.setId(3L);
            history.setOperatorRole(2);
            history.setOperatorRoleDesc("系统");
            history.setOperatorName("系统");
            history.setOperatorAvatar("");
            history.setActionType("退款成功");
            history.setContent("退款已成功处理，退款金额已原路退回");
            history.setRefundAmount(refund.getRefundAmount() != null ? refund.getRefundAmount().toString() : null);
            history.setOperateTime(refund.getRefundFinishTime());
            historyList.add(history);
        }

        return historyList;
    }

    private String getUserName(Long userId) {
        if (userId == null) return "";
        User user = userMapper.selectById(userId);
        return user != null ? user.getLoginAccount() : "";
    }
}
