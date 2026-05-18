package com.example.productmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.productmanagement.common.StatusEnum;
import com.example.productmanagement.entity.*;
import com.example.productmanagement.mapper.*;
import com.example.productmanagement.service.NotificationService;
import com.example.productmanagement.utils.SseEmitterManager;
import com.example.productmanagement.vo.NotificationVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final SseEmitterManager sseEmitterManager;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderRefundMapper orderRefundMapper;

    public NotificationServiceImpl(NotificationMapper notificationMapper,
                                   SseEmitterManager sseEmitterManager,
                                   UserMapper userMapper,
                                   OrderMapper orderMapper,
                                   OrderItemMapper orderItemMapper,
                                   OrderRefundMapper orderRefundMapper) {
        this.notificationMapper = notificationMapper;
        this.sseEmitterManager = sseEmitterManager;
        this.userMapper = userMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderRefundMapper = orderRefundMapper;
    }

    @Override
    @Async
    public void sendNotification(Notification notification) {
        notification.setStatus(StatusEnum.NotificationStatus.UNREAD.getCode());
        notificationMapper.insert(notification);

        sseEmitterManager.sendToUser(notification.getReceiverId(), notification.getReceiverType(), notification);

        log.info("发送通知: receiverId={}, type={}, title={}",
                notification.getReceiverId(), notification.getNotificationType(), notification.getTitle());
    }

    @Override
    public void notifySellerOrderPaid(String orderNo, Long userId) {
        List<Long> adminIds = getAdminIds();

        for (Long adminId : adminIds) {
            Notification notification = new Notification();
            notification.setNotificationType(StatusEnum.NotificationType.REMIND_SHIP.getCode());
            notification.setTitle("新订单通知");
            notification.setContent("您有新的订单需要发货，订单号：" + orderNo);
            notification.setReceiverId(adminId);
            notification.setReceiverType(StatusEnum.ReceiverType.SELLER.getCode());
            notification.setBusinessType(StatusEnum.BusinessType.ORDER.getCode());
            notification.setBusinessId(orderNo);

            sendNotification(notification);
        }
    }

    @Override
    public void notifySellerOrderCompleted(String orderNo, Long userId) {
        List<Long> adminIds = getAdminIds();

        for (Long adminId : adminIds) {
            Notification notification = new Notification();
            notification.setNotificationType(StatusEnum.NotificationType.ORDER_PROGRESS.getCode());
            notification.setTitle("交易完成通知");
            notification.setContent("订单 " + orderNo + " 已完成，交易成功");
            notification.setReceiverId(adminId);
            notification.setReceiverType(StatusEnum.ReceiverType.SELLER.getCode());
            notification.setBusinessType(StatusEnum.BusinessType.ORDER.getCode());
            notification.setBusinessId(orderNo);

            sendNotification(notification);
        }
    }

    @Override
    public void notifySellerOrderCancelled(String orderNo, Long userId) {
        List<Long> adminIds = getAdminIds();

        for (Long adminId : adminIds) {
            Notification notification = new Notification();
            notification.setNotificationType(StatusEnum.NotificationType.ORDER_PROGRESS.getCode());
            notification.setTitle("用户订单取消通知");
            notification.setContent("订单 " + orderNo + "已取消");
            notification.setReceiverId(adminId);
            notification.setReceiverType(StatusEnum.ReceiverType.SELLER.getCode());
            notification.setBusinessType(StatusEnum.BusinessType.ORDER.getCode());
            notification.setBusinessId(orderNo);

            sendNotification(notification);
        }
    }

    @Override
    public void notifySellerRefundApply(String refundNo, Long userId, BigDecimal refundAmount) {
        List<Long> adminIds = getAdminIds();

        for (Long adminId : adminIds) {
            Notification notification = new Notification();
            notification.setNotificationType(StatusEnum.NotificationType.REMIND_REFUND.getCode());
            notification.setTitle("退款申请通知");
            notification.setContent(String.format("用户申请退款，退款单号：%s，退款金额：%.2f元，请及时处理", refundNo, refundAmount));
            notification.setReceiverId(adminId);
            notification.setReceiverType(StatusEnum.ReceiverType.SELLER.getCode());
            notification.setBusinessType(StatusEnum.BusinessType.REFUND.getCode());
            notification.setBusinessId(refundNo);

            sendNotification(notification);
        }
    }

    @Override
    public void notifyUserOrderShipped(String orderNo, Long userId, List<String> bookNames) {
        String booksStr = formatBookNames(bookNames);

        Notification notification = new Notification();
        notification.setNotificationType(StatusEnum.NotificationType.ORDER_PROGRESS.getCode());
        notification.setTitle("订单发货通知");
        notification.setContent("您购买的" + booksStr + "已发货，订单号：" + orderNo);
        notification.setReceiverId(userId);
        notification.setReceiverType(StatusEnum.ReceiverType.USER.getCode());
        notification.setBusinessType(StatusEnum.BusinessType.ORDER.getCode());
        notification.setBusinessId(orderNo);

        sendNotification(notification);
    }

    @Override
    public void notifyUserRefundResult(String refundNo, Long userId, boolean approved, BigDecimal refundAmount) {
        Notification notification = new Notification();
        notification.setNotificationType(StatusEnum.NotificationType.ORDER_PROGRESS.getCode());

        if (approved) {
            notification.setTitle("退款成功通知");
            notification.setContent(String.format("您的退款申请已通过，退款金额：%.2f元，退款单号：%s", refundAmount, refundNo));
        } else {
            notification.setTitle("退款拒绝通知");
            notification.setContent("您的退款申请已被拒绝，退款单号：" + refundNo);
        }

        notification.setReceiverId(userId);
        notification.setReceiverType(StatusEnum.ReceiverType.USER.getCode());
        notification.setBusinessType(StatusEnum.BusinessType.REFUND.getCode());
        notification.setBusinessId(refundNo);

        sendNotification(notification);
    }

    @Override
    public void notifyUserRefund(String refundNo, Long userId, BigDecimal refundAmount) {
        Notification notification = new Notification();
        notification.setNotificationType(StatusEnum.NotificationType.ORDER_PROGRESS.getCode());

        notification.setTitle("退款成功通知");
        notification.setContent(String.format("您的订单已成功退款，退款金额：%.2f元，退款单号：%s", refundAmount, refundNo));

        notification.setReceiverId(userId);
        notification.setReceiverType(StatusEnum.ReceiverType.USER.getCode());
        notification.setBusinessType(StatusEnum.BusinessType.REFUND.getCode());
        notification.setBusinessId(refundNo);

        sendNotification(notification);
    }

    @Override
    public Integer getUnreadCount(Long receiverId, Integer receiverType) {
        return notificationMapper.countUnread(receiverId, receiverType);
    }

    @Override
    public IPage<Notification> listNotifications(Long receiverId, Integer receiverType, Integer page, Integer size) {
        Page<Notification> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getReceiverId, receiverId)
                .eq(Notification::getReceiverType, receiverType)
                .orderByDesc(Notification::getCreateTime);
        return notificationMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public List<Notification> listAllNotifications(Long receiverId, Integer receiverType) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getReceiverId, receiverId)
                .eq(Notification::getReceiverType, receiverType)
                .orderByDesc(Notification::getCreateTime);
        return notificationMapper.selectList(wrapper);
    }

    @Override
    public IPage<NotificationVo> listNotificationVos(Long receiverId, Integer receiverType, Integer page, Integer size) {
        IPage<Notification> notificationPage = listNotifications(receiverId, receiverType, page, size);

        Page<NotificationVo> voPage = new Page<>(page, size);
        voPage.setTotal(notificationPage.getTotal());
        voPage.setRecords(convertToVoList(notificationPage.getRecords()));

        return voPage;
    }

    @Override
    public List<NotificationVo> listAllNotificationVos(Long receiverId, Integer receiverType) {
        List<Notification> notifications = listAllNotifications(receiverId, receiverType);
        return convertToVoList(notifications);
    }

    @Override
    public void markAsRead(Long notificationId) {
        notificationMapper.markAsRead(notificationId);
    }

    @Override
    public void markAllAsRead(Long receiverId, Integer receiverType) {
        notificationMapper.markAllAsRead(receiverId, receiverType);
    }

    private List<Long> getAdminIds() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(User::getId)
                .eq(User::getRole, 2);

        List<User> admins = userMapper.selectList(wrapper);
        return admins.stream().map(User::getId).collect(Collectors.toList());
    }

    private String formatBookNames(List<String> bookNames) {
        if (bookNames == null || bookNames.isEmpty()) {
            return "商品";
        }

        if (bookNames.size() <= 3) {
            return bookNames.stream()
                    .map(name -> "《" + name + "》")
                    .collect(Collectors.joining("，"));
        }

        String firstThree = bookNames.stream()
                .limit(3)
                .map(name -> "《" + name + "》")
                .collect(Collectors.joining("，"));

        return firstThree + "等" + bookNames.size() + "件商品";
    }

    private List<NotificationVo> convertToVoList(List<Notification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return new ArrayList<>();
        }

        return notifications.stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());
    }

    private NotificationVo convertToVo(Notification notification) {
        NotificationVo vo = new NotificationVo();
        vo.setId(notification.getId());
        vo.setSenderId(0L);
        vo.setSenderAvatar(getCoverUrl(notification));
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setType(notification.getNotificationType());
        vo.setIsRead(notification.getStatus());
        vo.setBizId(notification.getBusinessId());
        vo.setCreateTime(notification.getCreateTime());

        return vo;
    }

    private String getCoverUrl(Notification notification) {
        if (notification.getBusinessId() == null) {
            return null;
        }

        Integer businessType = notification.getBusinessType();
        String businessId = notification.getBusinessId();

        if (businessType == null) {
            return null;
        }

        if (businessType == StatusEnum.BusinessType.ORDER.getCode()) {
            Order order = orderMapper.selectByOrderNo(businessId);
            if (order != null) {
                LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(OrderItem::getOrderId, order.getOrderId())
                        .last("LIMIT 1");
                OrderItem item = orderItemMapper.selectOne(wrapper);
                if (item != null) {
                    return item.getCoverImageUrl();
                }
            }
        } else if (businessType == StatusEnum.BusinessType.REFUND.getCode()) {
            LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderRefund::getRefundNo, businessId);
            OrderRefund refund = orderRefundMapper.selectOne(wrapper);
            if (refund != null) {
                OrderItem item = orderItemMapper.selectById(refund.getOrderItemId());
                if (item != null) {
                    return item.getCoverImageUrl();
                }
            }
        }

        return null;
    }
}
