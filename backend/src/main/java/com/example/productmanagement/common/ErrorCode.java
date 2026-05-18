package com.example.productmanagement.common;

import lombok.Getter;

/**
 * 错误码枚举
 * 定义系统中所有错误情况的错误码和错误信息
 *
 * @author vibe coding
 * @since 2026-04-20
 */
@Getter
public enum ErrorCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 系统错误
     */
    SYSTEM_ERROR(500, "系统繁忙，请稍后重试"),

    /**
     * 参数错误
     */
    PARAM_ERROR(400, "请求参数错误"),

    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权，请先登录"),

    /**
     * 禁止访问
     */
    FORBIDDEN(403, "禁止访问，权限不足"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "请求的资源不存在"),

    /**
     * 请求方法不支持
     */
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),

    // ==================== 购物车模块错误码 (1000-1099) ====================

    /**
     * 购物车商品不存在
     */
    CART_ITEM_NOT_FOUND(1000, "购物车商品不存在"),

    /**
     * 购物车商品数量非法
     */
    CART_QUANTITY_INVALID(1001, "商品数量不能小于1"),

    /**
     * 购物车商品已存在
     */
    CART_ITEM_EXISTS(1002, "该商品已在购物车中"),

    /**
     * 购物车为空
     */
    CART_EMPTY(1003, "购物车为空"),

    /**
     * 无权限操作购物车
     */
    CART_NO_PERMISSION(1004, "无权操作该购物车商品"),

    // ==================== 订单模块错误码 (1100-1199) ====================

    /**
     * 订单不存在
     */
    ORDER_NOT_FOUND(1100, "订单不存在"),

    /**
     * 订单状态非法
     */
    ORDER_STATUS_INVALID(1101, "订单状态非法"),

    /**
     * 订单状态流转错误
     */
    ORDER_STATUS_FLOW_ERROR(1102, "订单状态流转错误"),

    /**
     * 无权限操作订单
     */
    ORDER_NO_PERMISSION(1103, "无权操作该订单"),

    /**
     * 订单已取消
     */
    ORDER_ALREADY_CANCELLED(1104, "订单已取消"),

    /**
     * 订单已完成
     */
    ORDER_ALREADY_COMPLETED(1105, "订单已完成"),

    /**
     * 订单已支付
     */
    ORDER_ALREADY_PAID(1106, "订单已支付"),

    /**
     * 订单未支付
     */
    ORDER_NOT_PAID(1107, "订单未支付"),

    /**
     * 订单生成失败
     */
    ORDER_CREATE_FAILED(1108, "订单生成失败"),

    /**
     * 订单商品库存不足
     */
    ORDER_STOCK_INSUFFICIENT(1109, "商品库存不足"),

    /**
     * 订单商品已下架
     */
    ORDER_BOOK_OFF_SHELF(1110, "订单商品已下架"),

    /**
     * 收货信息不完整
     */
    ORDER_RECEIVER_INFO_INCOMPLETE(1111, "收货信息不完整"),

    /**
     * 订单明细为空
     */
    ORDER_ITEM_EMPTY(1112, "订单明细为空"),

    // ==================== 支付模块错误码 (1200-1299) ====================

    /**
     * 支付失败
     */
    PAYMENT_FAILED(1200, "支付失败"),

    /**
     * 支付记录不存在
     */
    PAYMENT_NOT_FOUND(1201, "支付记录不存在"),

    /**
     * 支付金额不匹配
     */
    PAYMENT_AMOUNT_MISMATCH(1202, "支付金额不匹配"),

    /**
     * 支付方式不支持
     */
    PAYMENT_METHOD_NOT_SUPPORT(1203, "支付方式不支持"),

    /**
     * 重复支付
     */
    PAYMENT_DUPLICATE(1204, "重复支付"),

    /**
     * 退款失败
     */
    REFUND_FAILED(1205, "退款失败"),

    /**
     * 退款金额超过支付金额
     */
    REFUND_AMOUNT_EXCEED(1206, "退款金额超过支付金额"),

    // ==================== 物流模块错误码 (1300-1399) ====================

    /**
     * 物流单号不存在
     */
    LOGISTICS_NOT_FOUND(1300, "物流单号不存在"),

    /**
     * 物流信息查询失败
     */
    LOGISTICS_QUERY_FAILED(1301, "物流信息查询失败"),

    // ==================== 用户模块错误码 (1400-1499) ====================

    /**
     * 用户不存在
     */
    USER_NOT_FOUND(1400, "用户不存在"),

    /**
     * 用户被禁用
     */
    USER_DISABLED(1401, "用户账号已被禁用"),

    // ==================== 商品模块错误码 (1500-1599) ====================

    /**
     * 商品不存在
     */
    BOOK_NOT_FOUND(1500, "商品不存在"),

    /**
     * 商品已下架
     */
    BOOK_OFF_SHELF(1501, "商品已下架"),

    // ==================== 用户地址模块 (1600-1699) ====================
    USER_ADDRESS_NOT_FOUND(1600, "用户地址不存在"),

    // ==================== 退款模块 (1700-1799) ====================
    REFUND_TYPE_ERROR(1700, "退款请求不合法"),
    REFUND_STATUS_ERROR(1701, "退款请求状态不合法");

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误信息
     */
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据错误码获取错误信息
     *
     * @param code 错误码
     * @return 错误信息
     */
    public static String getMessageByCode(Integer code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode.getMessage();
            }
        }
        return "未知错误";
    }
}
