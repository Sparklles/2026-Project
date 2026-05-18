package com.example.productmanagement.controller.front;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.productmanagement.common.Result;
import com.example.productmanagement.service.NotificationService;
import com.example.productmanagement.utils.UserHolder;
import com.example.productmanagement.vo.NotificationVo;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
@Validated
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(
            @NotNull @RequestParam Integer receiverType) {
        Long receiverId = UserHolder.getUserId();
        return Result.success(notificationService.getUnreadCount(receiverId, receiverType));
    }

//    @GetMapping("/list")
//    public Result<IPage<Notification>> listNotifications(
//            @NotNull @RequestParam Long receiverId,
//            @NotNull @RequestParam Integer receiverType,
//            @RequestParam(defaultValue = "1") Integer page,
//            @RequestParam(defaultValue = "10") Integer size) {
//        return Result.success(notificationService.listNotifications(receiverId, receiverType, page, size));
//    }
//
//    @GetMapping("/list/all")
//    public Result<List<Notification>> listAllNotifications(
//            @NotNull @RequestParam Long receiverId,
//            @NotNull @RequestParam Integer receiverType) {
//        return Result.success(notificationService.listAllNotifications(receiverId, receiverType));
//    }

    @GetMapping("/vo/list")
    public Result<IPage<NotificationVo>> listNotificationVos(
            @NotNull @RequestParam Integer receiverType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long receiverId = UserHolder.getUserId();
        return Result.success(notificationService.listNotificationVos(receiverId, receiverType, page, size));
    }

    @GetMapping("/vo/list/all")
    public Result<List<NotificationVo>> listAllNotificationVos(
            @NotNull @RequestParam Integer receiverType) {
        Long receiverId = UserHolder.getUserId();
        return Result.success(notificationService.listAllNotificationVos(receiverId, receiverType));
    }

    @PutMapping("/read/{id}")
    public Result<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success("已标记为已读");
    }

    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(
            @NotNull @RequestParam Integer receiverType) {
        Long receiverId = UserHolder.getUserId();
        notificationService.markAllAsRead(receiverId, receiverType);
        return Result.success("已全部标记为已读");
    }
}
