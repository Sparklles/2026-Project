package com.example.productmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.example.productmanagement.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * 统计指定用户购买某本书的有效记录数 (只统计已支付或已完成的订单)
     */
    int countPurchasedRecord(@Param("userId") Long userId, @Param("bookId") Long bookId);

    int insertBatch(List<OrderItem> list);

    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);

    @Select("SELECT " +
            "  oi.book_id, " +
            "  COALESCE(NULLIF(oi.book_name, ''), b.title) AS book_name, " +
            "  b.isbn, " +
            "  b.author, " +
            "  '' AS publisher, " +
            "  COALESCE(c.name, '未分类') AS category, " +
            "  COALESCE(SUM(oi.quantity), 0) AS total_quantity, " +
            "  COALESCE(SUM(oi.total_price), 0) AS total_sales " +
            "FROM order_item oi " +
            "INNER JOIN `order` o ON oi.order_id = o.order_id " +
            "LEFT JOIN book_info b ON oi.book_id = b.id " +
            "LEFT JOIN book_category c ON b.category_id = c.id " +
            "WHERE o.deleted = 0 AND o.order_status = 4 AND o.pay_status = 2 " +
            "GROUP BY oi.book_id, COALESCE(NULLIF(oi.book_name, ''), b.title), b.isbn, b.author, c.name " +
            "ORDER BY total_quantity DESC, total_sales DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getTopSalesBooks(@Param("limit") Integer limit);

    @Select("SELECT " +
            "  COALESCE(c.name, '未分类') AS category, " +
            "  COALESCE(SUM(oi.quantity), 0) AS total_quantity, " +
            "  COALESCE(SUM(oi.total_price), 0) AS total_sales, " +
            "  COUNT(DISTINCT oi.book_id) AS book_count " +
            "FROM order_item oi " +
            "INNER JOIN `order` o ON oi.order_id = o.order_id " +
            "LEFT JOIN book_info b ON oi.book_id = b.id " +
            "LEFT JOIN book_category c ON b.category_id = c.id " +
            "WHERE o.deleted = 0 AND o.order_status = 4 AND o.pay_status = 2 " +
            "GROUP BY COALESCE(c.name, '未分类') " +
            "ORDER BY total_quantity DESC, total_sales DESC")
    List<Map<String, Object>> getTopSalesCategories();

    @Select("SELECT " +
            "  COALESCE(NULLIF(b.author, ''), '未知作者') AS author, " +
            "  COUNT(DISTINCT oi.book_id) AS book_count, " +
            "  COALESCE(SUM(oi.quantity), 0) AS total_quantity, " +
            "  COALESCE(SUM(oi.total_price), 0) AS total_sales " +
            "FROM order_item oi " +
            "INNER JOIN `order` o ON oi.order_id = o.order_id " +
            "LEFT JOIN book_info b ON oi.book_id = b.id " +
            "WHERE o.deleted = 0 AND o.order_status = 4 AND o.pay_status = 2 " +
            "GROUP BY COALESCE(NULLIF(b.author, ''), '未知作者') " +
            "ORDER BY total_quantity DESC, total_sales DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getTopSalesAuthors(@Param("limit") Integer limit);

    @Select("SELECT " +
            "  DATE_FORMAT(o.create_time, '%Y-%m') AS stat_month, " +
            "  COALESCE(c.name, '未分类') AS category, " +
            "  COALESCE(SUM(oi.quantity), 0) AS total_quantity, " +
            "  COALESCE(SUM(oi.total_price), 0) AS total_sales " +
            "FROM order_item oi " +
            "INNER JOIN `order` o ON oi.order_id = o.order_id " +
            "LEFT JOIN book_info b ON oi.book_id = b.id " +
            "LEFT JOIN book_category c ON b.category_id = c.id " +
            "WHERE o.deleted = 0 AND o.order_status = 4 AND o.pay_status = 2 " +
            "GROUP BY DATE_FORMAT(o.create_time, '%Y-%m'), COALESCE(c.name, '未分类') " +
            "ORDER BY stat_month DESC, total_quantity DESC")
    List<Map<String, Object>> getCategorySalesTrend();
}
