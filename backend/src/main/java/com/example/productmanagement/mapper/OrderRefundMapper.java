package com.example.productmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.productmanagement.entity.OrderRefund;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderRefundMapper extends BaseMapper<OrderRefund> {

    /**
     * 检查订单商品是否存在退款记录
     *
     * @param orderItemId 订单商品ID
     * @return 存在返回true，不存在返回false
     */
    @Select("SELECT COUNT(*) > 0 FROM order_refund WHERE order_item_id = #{orderItemId}")
    boolean existsByOrderItemId(@Param("orderItemId") Long orderItemId);

    /**
     * 检查订单商品是否存在进行中的退款申请
     * 进行中状态：0-待审核、1-审核通过、2-用户已寄回、3-商家已收货
     *
     * @param orderItemId 订单商品ID
     * @return 存在返回true，不存在返回false
     */
    @Select("SELECT COUNT(*) > 0 FROM order_refund WHERE order_item_id = #{orderItemId} AND refund_status IN (0, 1, 2, 3)")
    boolean existsPendingRefundByOrderItemId(@Param("orderItemId") Long orderItemId);
}
