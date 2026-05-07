package com.example.productmanagement.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户状态审计日志 VO
 */
@Data
public class UserStatusAuditLogVo {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    private String targetLoginAccount;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long adminId;

    private String adminLoginAccount;

    private String action;

    private Integer beforeStatus;

    private Integer afterStatus;

    private String reasonType;

    private String reasonTypeName;

    private String reasonDetail;

    private LocalDateTime createTime;
}
