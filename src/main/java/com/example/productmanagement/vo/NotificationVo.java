package com.example.productmanagement.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationVo {
    // 1. 基础信息
    /**
     * 消息主键ID (用于前端调用"标记已读"或"删除消息"接口)
     */
    private Long id;

    /**
     * 发送者ID (系统消息可为0，如果是买卖双方未来聊天，则为对方的userId)
     */
    private Long senderId;

    /**
     * 发送者头像URL (用于列表左侧的图标/头像展示)
     */
    private String senderAvatar;

    // 2. 消息内容
    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息正文
     */
    private String content;

    // 3. 状态与类型
    /**
     * 消息类型：1-物流通知, 2-退款通知, 3-系统公告, 4-私信聊天
     * (前端根据此类型决定展示什么默认Icon，或者决定点击后的路由跳转逻辑)
     */
    private Integer type;

    /**
     * 阅读状态：0-未读，1-已读
     */
    private Integer isRead;

    // 4. 业务关联 (路由跳转关键)
    /**
     * 业务关联ID (例如：订单号 orderNo，退款单号 refundNo)
     * (前端拿到这个值后，结合 type 就可以实现点击消息跳转到特定订单详情)
     */
    private String bizId;

    // 5. 时间
    /**
     * 创建时间
     * (使用 @JsonFormat 确保后端返回的时间格式化规范，前端也可以直接使用)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
