package com.example.productmanagement.enums;

import lombok.Getter;

/**
 * 书籍列表排序字段枚举
 * 值对应数据库 book_info 表中的列名
 */
@Getter
public enum BookSortField {

    SALES("sales", "销量"),
    FAVORITE_COUNT("favorite_count", "收藏量"),
    AVG_RATING("avg_rating", "评分"),
    PRICE("price", "价格"),
    PUBLISH_DATE("publish_date", "出版日期");

    private final String column;   // 数据库列名
    private final String desc;     // 中文描述

    BookSortField(String column, String desc) {
        this.column = column;
        this.desc = desc;
    }

    /**
     * 根据前端传回的字段名查找枚举，找不到返回 null
     */
    public static BookSortField fromField(String field) {
        if (field == null) return null;
        for (BookSortField value : values()) {
            if (value.getColumn().equals(field)) {
                return value;
            }
        }
        return null;
    }
}