package com.example.productmanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.productmanagement.entity.Notification;
import com.example.productmanagement.vo.NotificationVo;

import java.math.BigDecimal;
import java.util.List;

public interface NotificationService {

    void sendNotification(Notification notification);

    void notifySellerOrderPaid(String orderNo, Long userId);

    void notifySellerOrderCompleted(String orderNo, Long userId);

    void notifySellerOrderCancelled(String orderNo, Long userId);

    void notifySellerRefundApply(String refundNo, Long userId, BigDecimal refundAmount);

    void notifyUserOrderShipped(String orderNo, Long userId, List<String> bookNames);

    void notifyUserRefundResult(String refundNo, Long userId, boolean approved, BigDecimal refundAmount);

    void notifyUserRefund(String refundNo, Long userId, BigDecimal refundAmount);

    Integer getUnreadCount(Long receiverId, Integer receiverType);

    IPage<Notification> listNotifications(Long receiverId, Integer receiverType, Integer page, Integer size);

    List<Notification> listAllNotifications(Long receiverId, Integer receiverType);

    IPage<NotificationVo> listNotificationVos(Long receiverId, Integer receiverType, Integer page, Integer size);

    List<NotificationVo> listAllNotificationVos(Long receiverId, Integer receiverType);

    void markAsRead(Long notificationId);

    void markAllAsRead(Long receiverId, Integer receiverType);
}
