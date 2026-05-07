package com.example.productmanagement.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 状态枚举类
 * 包含订单状态、支付状态、支付方式等枚举
 *
 * @author vibe coding
 * @since 2026-04-20
 */
public class StatusEnum {

    /**
     * 订单状态枚举
     */
    @Getter
    public enum OrderStatus {
        PENDING_PAY(1, "待支付"),
        PENDING_SHIP(2, "待发货"),
        SHIPPED(3, "已发货/待收货"),
        COMPLETED(4, "已完成"),
        CANCELED(5, "已取消"),
        AFTER_SALE(6, "售后中"),
        PENDING_SIGN(7, "待签收"),
        REFUND(8, "已退款");

        private final Integer code;
        private final String desc;

        OrderStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static String getDescByCode(Integer code) {
            for (OrderStatus status : values()) {
                if (status.getCode().equals(code)) {
                    return status.getDesc();
                }
            }
            return "未知状态";
        }
    }

    /**
     * 支付状态枚举
     */
    @Getter
    public enum PayStatus {
        UNPAID(0, "未支付"),
        PAYING(1, "支付中"),
        PAYED(2,"已支付");


        private final Integer code;
        private final String desc;

        PayStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static String getDescByCode(Integer code) {
            for (PayStatus status : values()) {
                if (status.getCode().equals(code)) {
                    return status.getDesc();
                }
            }
            return "未知状态";
        }
    }

    /**
     * 支付方式枚举
     */
    @Getter
    public enum PayType {
        ALIPAY(1, "支付宝"),
        WECHAT(2, "微信支付"),
        BANK_CARD(3, "银行卡");

        private final Integer code;
        private final String desc;

        PayType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static String getDescByCode(Integer code) {
            for (PayType type : values()) {
                if (type.getCode().equals(code)) {
                    return type.getDesc();
                }
            }
            return "未知方式";
        }
    }

    /**
     * 物流状态枚举
     */
    @Getter
    public enum LogisticsStatus {
        PENDING(0, "待发货"),
        PICKED_UP(1, "已揽收"),
        IN_TRANSIT(2, "运输中"),
        OUT_FOR_DELIVERY(3, "派件中"),
        DELIVERED(4, "已签收"),
        ERROR(5, "异常");

        private final Integer code;
        private final String desc;

        LogisticsStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static String getDescByCode(Integer code) {
            for (LogisticsStatus status : values()) {
                if (status.getCode().equals(code)) {
                    return status.getDesc();
                }
            }
            return "未知状态";
        }
    }


    /**
     *  商品上架状态
     */
    @Getter
    @AllArgsConstructor
    public enum BookStatus {

        ON_SHELF(0, "下架"),
        OFF_SHELF(1, "上架");

        private Integer code = 0;
        private String desc;

        public static String getDescByCode(Integer code) {
            for (BookStatus status : values()) {
                if (status.getCode().equals(code)) {
                    return status.getDesc();
                }
            }

            return "未知状态";
        }
    }
}
