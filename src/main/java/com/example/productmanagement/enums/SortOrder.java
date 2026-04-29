package com.example.productmanagement.enums;

import lombok.Getter;

/**
 * 排序方向枚举
 */
@Getter
public enum SortOrder {

    ASC("asc", "升序"),
    DESC("desc", "降序");

    private final String value;
    private final String desc;

    SortOrder(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    /**
     * 根据字符串值查找对应的枚举，忽略大小写
     */
    public static SortOrder fromValue(String value) {
        if (value == null) return null;
        for (SortOrder order : values()) {
            if (order.getValue().equalsIgnoreCase(value)) {
                return order;
            }
        }
        return null;
    }
}