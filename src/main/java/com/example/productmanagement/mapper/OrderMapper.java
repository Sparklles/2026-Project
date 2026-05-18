package com.example.productmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.productmanagement.dto.OrderQueryDto;
import com.example.productmanagement.entity.Order;
import com.example.productmanagement.vo.OrderListVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    IPage<Order> selectOrdersByUserId(@Param("page") Page<Order> page, @Param("userId") Long userId);

    List<Order> selectOrdersByUserId(@Param("userId") Long userId);

    Order selectByOrderNo(@Param("orderNo") String orderNo);

    int softDelete(@Param("orderId") Long orderId, @Param("userId") Long userId);

    IPage<OrderListVo> selectOrdersByDynamicQuery(@Param("page") Page<OrderListVo> page,
                                                  @Param("query") OrderQueryDto query);

    List<OrderListVo> selectOrdersByDynamicQuery(@Param("query") OrderQueryDto query);

    @Select("SELECT " +
            "  COUNT(*) AS total_orders, " +
            "  COALESCE(SUM(total_amount), 0) AS total_amount, " +
            "  COALESCE(SUM(pay_amount), 0) AS paid_amount, " +
            "  COALESCE(AVG(pay_amount), 0) AS avg_amount " +
            "FROM `order` " +
            "WHERE deleted = 0 AND order_status = 4 AND pay_status = 2")
    Map<String, Object> getSalesSummary();

    @Select("SELECT " +
            "  DATE(create_time) AS stat_date, " +
            "  COUNT(*) AS order_count, " +
            "  COALESCE(SUM(total_amount), 0) AS total_sales, " +
            "  COALESCE(SUM(pay_amount), 0) AS actual_sales " +
            "FROM `order` " +
            "WHERE deleted = 0 AND order_status = 4 AND pay_status = 2 " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY stat_date")
    List<Map<String, Object>> getSalesByDate();

    @Select("SELECT " +
            "  DATE(create_time) AS stat_date, " +
            "  COUNT(*) AS order_count, " +
            "  COALESCE(SUM(total_amount), 0) AS total_sales, " +
            "  COALESCE(SUM(pay_amount), 0) AS actual_sales " +
            "FROM `order` " +
            "WHERE deleted = 0 AND order_status = 4 AND pay_status = 2 " +
            "  AND (#{startDate} IS NULL OR #{startDate} = '' OR DATE(create_time) >= #{startDate}) " +
            "  AND (#{endDate} IS NULL OR #{endDate} = '' OR DATE(create_time) <= #{endDate}) " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY stat_date")
    List<Map<String, Object>> getSalesByDateRange(@Param("startDate") String startDate,
                                                  @Param("endDate") String endDate);

    @Select("SELECT " +
            "  COUNT(*) AS total_orders, " +
            "  COALESCE(SUM(total_amount), 0) AS total_amount, " +
            "  COALESCE(SUM(pay_amount), 0) AS paid_amount, " +
            "  COALESCE(AVG(pay_amount), 0) AS avg_amount " +
            "FROM `order` " +
            "WHERE deleted = 0 AND order_status = 4 AND pay_status = 2 " +
            "  AND (#{startDate} IS NULL OR #{startDate} = '' OR DATE(create_time) >= #{startDate}) " +
            "  AND (#{endDate} IS NULL OR #{endDate} = '' OR DATE(create_time) <= #{endDate})")
    Map<String, Object> getSalesSummaryByDateRange(@Param("startDate") String startDate,
                                                   @Param("endDate") String endDate);

    @Select("SELECT " +
            "  DATE_FORMAT(create_time, '%Y-%m') AS stat_month, " +
            "  COUNT(*) AS order_count, " +
            "  COALESCE(SUM(total_amount), 0) AS total_sales, " +
            "  COALESCE(SUM(pay_amount), 0) AS actual_sales " +
            "FROM `order` " +
            "WHERE deleted = 0 AND order_status = 4 AND pay_status = 2 " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m') " +
            "ORDER BY stat_month")
    List<Map<String, Object>> getSalesByMonth();

    @Select("SELECT COALESCE(SUM(oi.quantity), 0) " +
            "FROM order_item oi " +
            "INNER JOIN `order` o ON oi.order_id = o.order_id " +
            "WHERE o.deleted = 0 AND o.order_status = 4 AND o.pay_status = 2")
    Long getTotalQuantity();

    @Select("SELECT COALESCE(SUM(oi.quantity), 0) " +
            "FROM order_item oi " +
            "INNER JOIN `order` o ON oi.order_id = o.order_id " +
            "WHERE o.deleted = 0 AND o.order_status = 4 AND o.pay_status = 2 " +
            "  AND (#{startDate} IS NULL OR #{startDate} = '' OR DATE(o.create_time) >= #{startDate}) " +
            "  AND (#{endDate} IS NULL OR #{endDate} = '' OR DATE(o.create_time) <= #{endDate})")
    Long getTotalQuantityByDateRange(@Param("startDate") String startDate,
                                     @Param("endDate") String endDate);

    @Select("SELECT " +
            "  CASE order_status " +
            "    WHEN 1 THEN '待支付' " +
            "    WHEN 2 THEN '待发货' " +
            "    WHEN 3 THEN '已发货/待收货' " +
            "    WHEN 4 THEN '已完成' " +
            "    WHEN 5 THEN '已取消' " +
            "    WHEN 6 THEN '售后中' " +
            "    WHEN 7 THEN '待签收' " +
            "    WHEN 8 THEN '已退款' " +
            "    ELSE '未知状态' " +
            "  END AS status_name, " +
            "  COUNT(*) AS order_count, " +
            "  COALESCE(SUM(total_amount), 0) AS total_amount " +
            "FROM `order` " +
            "WHERE deleted = 0 " +
            "GROUP BY order_status " +
            "ORDER BY order_status")
    List<Map<String, Object>> countOrdersByStatus();

    @Select("SELECT " +
            "  CASE pay_type " +
            "    WHEN 1 THEN '支付宝' " +
            "    WHEN 2 THEN '微信支付' " +
            "    WHEN 3 THEN '银行卡' " +
            "    ELSE '其他' " +
            "  END AS pay_type_name, " +
            "  COUNT(*) AS order_count, " +
            "  COALESCE(SUM(pay_amount), 0) AS total_amount " +
            "FROM `order` " +
            "WHERE deleted = 0 AND pay_status = 2 " +
            "GROUP BY pay_type " +
            "ORDER BY order_count DESC")
    List<Map<String, Object>> countOrdersByPayType();
}

