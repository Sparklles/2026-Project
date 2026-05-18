package com.example.productmanagement.service.impl;

import com.example.productmanagement.dto.*;
import com.example.productmanagement.mapper.BookInfoMapper;
import com.example.productmanagement.mapper.OrderItemMapper;
import com.example.productmanagement.mapper.OrderMapper;
import com.example.productmanagement.mapper.SearchLogMapper;
import com.example.productmanagement.mapper.UserMapper;
import com.example.productmanagement.service.StatisticsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SearchLogMapper searchLogMapper;

    @Autowired
    private BookInfoMapper bookInfoMapper;

    @Override
    public SalesSummaryDTO getSalesSummary() {
        Map<String, Object> summaryMap = orderMapper.getSalesSummary();
        SalesSummaryDTO summary = new SalesSummaryDTO();
        if (summaryMap != null) {
            summary.setTotalOrders(toLong(summaryMap.get("total_orders")));
            summary.setTotalAmount(toBigDecimal(summaryMap.get("total_amount")));
            summary.setPaidAmount(toBigDecimal(summaryMap.get("paid_amount")));
            summary.setTotalSales(summary.getPaidAmount());
            summary.setAvgAmount(toBigDecimal(summaryMap.get("avg_amount")));
            summary.setAverageOrderValue(summary.getAvgAmount());
        }
        Long userCount = userMapper.countActiveUsers();
        summary.setUserCount(userCount != null ? userCount : 0L);
        Long totalQuantity = orderMapper.getTotalQuantity();
        summary.setTotalQuantity(totalQuantity != null ? totalQuantity : 0L);
        return summary;
    }

    @Override
    public List<SalesDTO> getSalesByDate() {
        List<Map<String, Object>> list = orderMapper.getSalesByDate();
        List<SalesDTO> result = new ArrayList<>();
        for (Map<String, Object> map : list) {
            result.add(toSalesDTO(map, "stat_date"));
        }
        return result;
    }

    @Override
    public List<SalesDTO> getSalesByDateRange(String startDate, String endDate) {
        List<Map<String, Object>> list = orderMapper.getSalesByDateRange(startDate, endDate);
        List<SalesDTO> result = new ArrayList<>();
        for (Map<String, Object> map : list) {
            result.add(toSalesDTO(map, "stat_date"));
        }
        return result;
    }

    @Override
    public SalesSummaryDTO getSalesSummaryByDateRange(String startDate, String endDate) {
        Map<String, Object> summaryMap = orderMapper.getSalesSummaryByDateRange(startDate, endDate);
        SalesSummaryDTO summary = new SalesSummaryDTO();
        if (summaryMap != null) {
            summary.setTotalOrders(toLong(summaryMap.get("total_orders")));
            summary.setTotalAmount(toBigDecimal(summaryMap.get("total_amount")));
            summary.setPaidAmount(toBigDecimal(summaryMap.get("paid_amount")));
            summary.setTotalSales(summary.getPaidAmount());
            summary.setAvgAmount(toBigDecimal(summaryMap.get("avg_amount")));
            summary.setAverageOrderValue(summary.getAvgAmount());
        }
        Long totalQuantity = orderMapper.getTotalQuantityByDateRange(startDate, endDate);
        summary.setTotalQuantity(totalQuantity != null ? totalQuantity : 0L);
        return summary;
    }

    @Override
    public List<SalesDTO> getSalesByMonth() {
        List<Map<String, Object>> list = orderMapper.getSalesByMonth();
        List<SalesDTO> result = new ArrayList<>();
        for (Map<String, Object> map : list) {
            result.add(toSalesDTO(map, "stat_month"));
        }
        return result;
    }

    @Override
    public List<SalesRankDTO> getTopSalesBooks(Integer limit) {
        List<Map<String, Object>> list = orderItemMapper.getTopSalesBooks(normalizeLimit(limit));
        List<SalesRankDTO> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> map = list.get(i);
            SalesRankDTO dto = new SalesRankDTO();
            dto.setRank(i + 1);
            dto.setBookId(toNullableLong(map.get("book_id")));
            dto.setBookName(toNullableString(map.get("book_name")));
            dto.setIsbn(toNullableString(map.get("isbn")));
            dto.setAuthor(toNullableString(map.get("author")));
            dto.setPublisher(toNullableString(map.get("publisher")));
            dto.setCategory(toNullableString(map.get("category")));
            dto.setTotalQuantity(toInt(map.get("total_quantity")));
            dto.setTotalSales(toBigDecimal(map.get("total_sales")));
            result.add(dto);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getTopSalesCategories() {
        return orderItemMapper.getTopSalesCategories();
    }

    @Override
    public List<Map<String, Object>> getTopSalesAuthors(Integer limit) {
        return orderItemMapper.getTopSalesAuthors(normalizeLimit(limit));
    }

    @Override
    public List<UserProfileDTO> getUserAgeDistribution() {
        List<Map<String, Object>> list = userMapper.countUsersByAgeRange();
        return convertToUserProfileDTO(list, "年龄分布");
    }

    @Override
    public List<UserProfileDTO> getUserGenderDistribution() {
        List<Map<String, Object>> list = userMapper.countUsersByGender();
        return convertToUserProfileDTO(list, "性别分布");
    }

    @Override
    public List<UserProfileDTO> getUserSpentDistribution() {
        List<Map<String, Object>> list = userMapper.countUsersBySpentRange();
        return convertToUserProfileDTO(list, "消费区间");
    }

    @Override
    public List<UserProfileDTO> getUserRoleDistribution() {
        List<Map<String, Object>> list = userMapper.countUsersByRole();
        List<UserProfileDTO> result = new ArrayList<>();
        for (Map<String, Object> map : list) {
            UserProfileDTO dto = new UserProfileDTO();
            dto.setCategoryName(toNullableString(map.get("role_name")));
            dto.setCategoryValue("用户角色");
            dto.setUserCount(toLong(map.get("user_count")));
            result.add(dto);
        }
        return result;
    }

    @Override
    public List<SearchKeywordDTO> getHotSearchKeywords(Integer limit) {
        List<Map<String, Object>> list = searchLogMapper.getHotSearchKeywords(normalizeLimit(limit));
        List<SearchKeywordDTO> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> map = list.get(i);
            SearchKeywordDTO dto = new SearchKeywordDTO();
            dto.setRank(i + 1);
            dto.setKeyword(toNullableString(map.get("search_keyword")));
            dto.setSearchCount(toLong(map.get("search_count")));
            dto.setTotalResults(toLong(map.get("total_results")));
            result.add(dto);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getSearchStatsByDate() {
        return searchLogMapper.getSearchStatsByDate();
    }

    @Override
    public List<Map<String, Object>> getSearchResultDistribution() {
        return searchLogMapper.getSearchResultDistribution();
    }

    @Override
    public List<Map<String, Object>> getSearchHourlyTrend() {
        return searchLogMapper.getSearchHourlyTrend();
    }

    @Override
    public List<Map<String, Object>> getOrderStatusDistribution() {
        return orderMapper.countOrdersByStatus();
    }

    @Override
    public List<Map<String, Object>> getPayTypeDistribution() {
        return orderMapper.countOrdersByPayType();
    }

    @Override
    public List<Map<String, Object>> getCategorySalesTrend() {
        return orderItemMapper.getCategorySalesTrend();
    }

    @Override
    public List<Map<String, Object>> getBookCategoryStatistics() {
        return bookInfoMapper.countBooksByCategory();
    }

    @Override
    public List<Map<String, Object>> getBookPriceDistribution() {
        return bookInfoMapper.countBooksByPriceRange();
    }

    @Override
    public List<DailyUserReportDTO> getDailyUserReport() {
        List<Map<String, Object>> list = userMapper.getDailyNewUsers();
        List<DailyUserReportDTO> result = new ArrayList<>();

        // 建立日期到新用户数的映射，用于同比计算
        Map<String, Long> dateMap = new HashMap<>();
        for (Map<String, Object> map : list) {
            String date = String.valueOf(map.get("stat_date"));
            long newUsers = toLong(map.get("new_users"));
            dateMap.put(date, newUsers);
        }

        long cumulative = 0;
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> map = list.get(i);
            String date = String.valueOf(map.get("stat_date"));
            long newUsers = toLong(map.get("new_users"));
            cumulative += newUsers;

            // 环比：与前一天比较
            String momRate = "-";
            if (i > 0) {
                long prevUsers = toLong(list.get(i - 1).get("new_users"));
                if (prevUsers > 0) {
                    double rate = (newUsers - prevUsers) * 100.0 / prevUsers;
                    momRate = String.format("%.2f%%", rate);
                    if (rate > 0) momRate = "+" + momRate;
                }
            }

            // 同比：与去年同一天比较
            String yoyRate = "-";
            try {
                LocalDate currentDate = LocalDate.parse(date);
                LocalDate lastYearDate = currentDate.minusYears(1);
                String lastYearStr = lastYearDate.toString();
                Long lastYearUsers = dateMap.get(lastYearStr);
                if (lastYearUsers != null && lastYearUsers > 0) {
                    double rate = (newUsers - lastYearUsers) * 100.0 / lastYearUsers;
                    yoyRate = String.format("%.2f%%", rate);
                    if (rate > 0) yoyRate = "+" + yoyRate;
                }
            } catch (Exception e) {
                // ignore parse error
            }

            DailyUserReportDTO dto = new DailyUserReportDTO();
            dto.setStatDate(date);
            dto.setNewUsers(newUsers);
            dto.setCumulativeUsers(cumulative);
            dto.setMomRate(momRate);
            dto.setYoyRate(yoyRate);
            result.add(dto);
        }

        return result;
    }

    private List<UserProfileDTO> convertToUserProfileDTO(List<Map<String, Object>> list, String categoryValue) {
        return list.stream().map(map -> {
            UserProfileDTO dto = new UserProfileDTO();
            Object rangeObj = map.get("age_range") != null ? map.get("age_range") :
                              map.get("gender_name") != null ? map.get("gender_name") :
                              map.get("spent_range");
            dto.setCategoryName(rangeObj != null ? String.valueOf(rangeObj) : null);
            dto.setCategoryValue(categoryValue);
            dto.setUserCount(toLong(map.get("user_count")));
            return dto;
        }).collect(Collectors.toList());
    }

    private SalesDTO toSalesDTO(Map<String, Object> map, String dateKey) {
        SalesDTO dto = new SalesDTO();
        dto.setStatDate(toNullableString(map.get(dateKey)));
        dto.setOrderCount(toLong(map.get("order_count")));
        dto.setTotalSales(toBigDecimal(map.get("total_sales")));
        dto.setActualSales(toBigDecimal(map.get("actual_sales")));
        return dto;
    }

    private Integer normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 10;
        }
        return Math.min(limit, 100);
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return new BigDecimal(String.valueOf(value));
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private Long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private Long toNullableLong(Object value) {
        return value == null ? null : toLong(value);
    }

    private Integer toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String toNullableString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
