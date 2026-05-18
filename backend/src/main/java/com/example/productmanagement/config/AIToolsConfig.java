package com.example.productmanagement.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.productmanagement.entity.BookInfo;
import com.example.productmanagement.entity.ShippingAddress;
import com.example.productmanagement.mapper.BookInfoMapper;
import com.example.productmanagement.mapper.ShippingAddressMapper;
import com.example.productmanagement.service.OrderService;
import com.example.productmanagement.dto.OrderCreateRequest;
import com.example.productmanagement.dto.OrderItemDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class AIToolsConfig {

    // ================= 1. 定义入参 Record =================
    public record ProductSearchRequest(String keyword) {}
    public record OrderRequest(Long userId, Long bookId, Integer quantity) {}

    // ================= 2. 注册查询商品工具 =================
    @Bean
    @Description("航海商品查询助手：当用户询问有什么商品、书籍或装备时，调用此工具通过关键词模糊搜索数据库。")
    public Function<ProductSearchRequest, String> searchProductTool(BookInfoMapper bookInfoMapper) {
        return request -> {
            LambdaQueryWrapper<BookInfo> wrapper = new LambdaQueryWrapper<>();
            // 在 book_info 表中通过 title 和 description 模糊搜索商品[cite: 1]
            wrapper.like(BookInfo::getTitle, request.keyword())
                    .or()
                    .like(BookInfo::getDescription, request.keyword())
                    .eq(BookInfo::getStatus, 1) // 仅查询已上架的[cite: 1]
                    .last("LIMIT 3"); // 限制返回前3个，防止上下文Token溢出

            List<BookInfo> books = bookInfoMapper.selectList(wrapper);
            if (books.isEmpty()) {
                return "未找到相关航海商品，请建议用户换个关键词。";
            }

            // 将查询结果浓缩为字符串返回给大模型
            return books.stream()
                    .map(b -> String.format("ID:%d, 名称:%s, 价格:¥%s, 库存:%d",
                            b.getId(), b.getTitle(), b.getPrice().toString(), b.getStock()))
                    .collect(Collectors.joining(" | "));
        };
    }

    // ================= 3. 注册一键下单工具 =================
    @Bean
    @Description("一键下单助手：当用户明确表示要购买、下单某个商品时，调用此工具。必须传入 userId、商品 bookId 和购买数量 quantity。")
    public Function<OrderRequest, String> createOrderTool(
            OrderService orderService,
            ShippingAddressMapper addressMapper) {

        return request -> {
            // 1. 自动帮用户查找默认收货地址[cite: 1]
            LambdaQueryWrapper<ShippingAddress> addrWrapper = new LambdaQueryWrapper<>();
            addrWrapper.eq(ShippingAddress::getUserId, request.userId())
                    .eq(ShippingAddress::getIsDefault, 1)
                    .last("LIMIT 1");
            ShippingAddress defaultAddress = addressMapper.selectOne(addrWrapper);

            if (defaultAddress == null) {
                return "执行失败：用户尚未配置默认收货地址。请告诉用户前往个人中心配置地址后再下单。";
            }

            try {
                // 2. 组装下单参数，复用我们之前写好的健壮的 createOrder 逻辑
                OrderCreateRequest orderReq = new OrderCreateRequest();
                orderReq.setUserId(request.userId());
                orderReq.setAddressId(defaultAddress.getId());
                orderReq.setRemark("AI 智能语音/对话自动下单");

                OrderItemDTO item = new OrderItemDTO();
                item.setBookId(request.bookId());
                item.setQuantity(request.quantity());
                orderReq.setOrderItems(Collections.singletonList(item));

                // 3. 执行下单，向 order 和 order_item 表插入数据[cite: 1]
                String orderNo = orderService.createOrder(orderReq);
                return "下单成功！订单号为：" + orderNo + "。请回复用户订单已生成，并引导用户前往结算中心支付。";
            } catch (Exception e) {
                return "下单失败：" + e.getMessage() + "。请委婉地把失败原因转述给用户。";
            }
        };
    }
}
