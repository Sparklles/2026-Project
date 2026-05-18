package com.example.productmanagement.strategy;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.productmanagement.entity.*;
import com.example.productmanagement.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AlsoBoughtStrategy implements RecommendStrategy {

    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;
    private final BookInfoMapper bookInfoMapper;
    private final BookTagRelationMapper bookTagRelationMapper;

    @Override
    public String getType() {
        return "ORDER_SIMILARITY";
    }

    @Override
    public List<ScoredBook> execute(Map<String, Object> params) {
        int limit = StrategyLimits.limit(params.get("limit"), 10);
        Long sourceBookId = params.containsKey("sourceBookId") ? ((Number) params.get("sourceBookId")).longValue() : null;
        if (sourceBookId == null) {
            return new ArrayList<>();
        }

        Map<String, Double> weights = new HashMap<>();
        weights.put("coOccurOrder", 0.5);
        weights.put("sameUser", 0.3);
        weights.put("sameCategory", 0.1);
        weights.put("sameTag", 0.1);

        @SuppressWarnings("unchecked")
        Map<String, Object> wParam = params.containsKey("weights") ? (Map<String, Object>) params.get("weights") : null;
        if (wParam != null) {
            wParam.forEach((k, v) -> {
                if (v instanceof Number number) {
                    weights.put(k, number.doubleValue());
                }
            });
        }

        Map<Long, Double> scoreMap = new HashMap<>();
        Map<Long, String> reasonMap = new HashMap<>();

        computeCoOccurrence(sourceBookId, scoreMap, reasonMap, weights.get("coOccurOrder"));
        computeSameUser(sourceBookId, scoreMap, reasonMap, weights.get("sameUser"));
        computeSameCategory(sourceBookId, scoreMap, reasonMap, weights.get("sameCategory"));
        computeSameTag(sourceBookId, scoreMap, reasonMap, weights.get("sameTag"));

        List<Long> validBookIds = filterValidBooks(new ArrayList<>(scoreMap.keySet()));

        List<ScoredBook> result = new ArrayList<>();
        for (Long bookId : validBookIds) {
            Double rawScore = scoreMap.getOrDefault(bookId, 0.0);
            result.add(new ScoredBook(bookId,
                    BigDecimal.valueOf(rawScore).setScale(4, RoundingMode.HALF_UP),
                    reasonMap.getOrDefault(bookId, "关联")));
        }

        result.sort((a, b) -> b.getScore().compareTo(a.getScore()));
        if (result.size() > limit) {
            result = result.subList(0, limit);
        }
        return result;
    }

    private void computeCoOccurrence(Long sourceBookId, Map<Long, Double> scoreMap,
                                     Map<Long, String> reasonMap, double weight) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getBookId, sourceBookId);
        List<OrderItem> sourceItems = orderItemMapper.selectList(wrapper);
        Set<Long> orderIds = filterValidOrderIds(sourceItems.stream()
                .map(OrderItem::getOrderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        if (orderIds.isEmpty()) return;

        LambdaQueryWrapper<OrderItem> coWrapper = new LambdaQueryWrapper<>();
        coWrapper.in(OrderItem::getOrderId, orderIds)
                .ne(OrderItem::getBookId, sourceBookId);
        List<OrderItem> coItems = orderItemMapper.selectList(coWrapper);

        Map<Long, Long> countMap = coItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getBookId, Collectors.counting()));

        long maxCount = countMap.values().stream().max(Long::compareTo).orElse(1L);
        countMap.forEach((bookId, count) -> {
            double normalized = maxCount > 0 ? (double) count / maxCount : 0;
            scoreMap.merge(bookId, normalized * weight, Double::sum);
            reasonMap.merge(bookId, "同一订单购买", (a, b) -> a);
        });
    }

    private void computeSameUser(Long sourceBookId, Map<Long, Double> scoreMap,
                                 Map<Long, String> reasonMap, double weight) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getBookId, sourceBookId);
        List<OrderItem> sourceItems = orderItemMapper.selectList(wrapper);
        Set<Long> orderIds = filterValidOrderIds(sourceItems.stream()
                .map(OrderItem::getOrderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        if (orderIds.isEmpty()) return;

        List<Order> orders = orderMapper.selectBatchIds(orderIds);
        Set<Long> userIds = orders.stream()
                .filter(this::isValidOrder)
                .map(Order::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (userIds.isEmpty()) return;

        LambdaQueryWrapper<Order> userOrderWrapper = new LambdaQueryWrapper<>();
        userOrderWrapper.in(Order::getUserId, userIds)
                .eq(Order::getDeleted, 0)
                .eq(Order::getPayStatus, 2)
                .in(Order::getOrderStatus, List.of(2, 3, 4, 7));
        List<Order> userOrders = orderMapper.selectList(userOrderWrapper);
        Set<Long> allOrderIds = userOrders.stream().map(Order::getOrderId).collect(Collectors.toSet());
        if (allOrderIds.isEmpty()) return;

        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.in(OrderItem::getOrderId, allOrderIds)
                .ne(OrderItem::getBookId, sourceBookId);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

        Map<Long, Long> countMap = items.stream()
                .collect(Collectors.groupingBy(OrderItem::getBookId, Collectors.counting()));

        long maxCount = countMap.values().stream().max(Long::compareTo).orElse(1L);
        countMap.forEach((bookId, count) -> {
            double normalized = maxCount > 0 ? (double) count / maxCount : 0;
            scoreMap.merge(bookId, normalized * weight, Double::sum);
            reasonMap.merge(bookId, "同用户购买", (a, b) -> a);
        });
    }

    private void computeSameCategory(Long sourceBookId, Map<Long, Double> scoreMap,
                                     Map<Long, String> reasonMap, double weight) {
        BookInfo source = bookInfoMapper.selectById(sourceBookId);
        if (source == null || source.getCategoryId() == null) return;

        LambdaQueryWrapper<BookInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookInfo::getCategoryId, source.getCategoryId())
                .ne(BookInfo::getId, sourceBookId)
                .eq(BookInfo::getStatus, 1)
                .eq(BookInfo::getIsDeleted, 0);
        List<BookInfo> sameCat = bookInfoMapper.selectList(wrapper);

        for (BookInfo book : sameCat) {
            scoreMap.merge(book.getId(), 1.0 * weight, Double::sum);
            reasonMap.merge(book.getId(), "同分类", (a, b) -> a);
        }
    }

    private void computeSameTag(Long sourceBookId, Map<Long, Double> scoreMap,
                                Map<Long, String> reasonMap, double weight) {
        LambdaQueryWrapper<BookTagRelation> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(BookTagRelation::getBookId, sourceBookId);
        List<BookTagRelation> sourceTags = bookTagRelationMapper.selectList(tagWrapper);
        Set<Long> tagIds = sourceTags.stream().map(BookTagRelation::getTagId).collect(Collectors.toSet());

        if (tagIds.isEmpty()) return;

        LambdaQueryWrapper<BookTagRelation> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.in(BookTagRelation::getTagId, tagIds)
                .ne(BookTagRelation::getBookId, sourceBookId);
        List<BookTagRelation> relations = bookTagRelationMapper.selectList(relWrapper);

        Set<Long> bookIds = relations.stream().map(BookTagRelation::getBookId).collect(Collectors.toSet());

        for (Long bookId : bookIds) {
            scoreMap.merge(bookId, 1.0 * weight, Double::sum);
            reasonMap.merge(bookId, "同标签", (a, b) -> a);
        }
    }

    private List<Long> filterValidBooks(List<Long> bookIds) {
        if (bookIds.isEmpty()) return new ArrayList<>();
        LambdaQueryWrapper<BookInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(BookInfo::getId, bookIds)
                .eq(BookInfo::getStatus, 1)
                .eq(BookInfo::getIsDeleted, 0);
        List<BookInfo> valid = bookInfoMapper.selectList(wrapper);
        return valid.stream().map(BookInfo::getId).collect(Collectors.toList());
    }

    private Set<Long> filterValidOrderIds(Set<Long> orderIds) {
        if (orderIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<Order> orders = orderMapper.selectBatchIds(orderIds);
        return orders.stream()
                .filter(this::isValidOrder)
                .map(Order::getOrderId)
                .collect(Collectors.toSet());
    }

    private boolean isValidOrder(Order order) {
        if (order == null) {
            return false;
        }
        return Objects.equals(order.getDeleted(), 0)
                && Objects.equals(order.getPayStatus(), 2)
                && List.of(2, 3, 4, 7).contains(order.getOrderStatus());
    }
}
