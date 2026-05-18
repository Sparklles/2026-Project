package com.example.productmanagement.service;



import com.example.productmanagement.dto.*;

import java.util.List;
import java.util.Map;

/**
 * 统计服务接口
 */
public interface StatisticsService {

    /**
     * 获取销售汇总
     */
    SalesSummaryDTO getSalesSummary();

    /**
     * 按日期获取销售额统计
     */
    List<SalesDTO> getSalesByDate();

    /**
     * 按日期范围获取销售额统计
     */
    List<SalesDTO> getSalesByDateRange(String startDate, String endDate);

    /**
     * 按日期范围获取销售汇总
     */
    SalesSummaryDTO getSalesSummaryByDateRange(String startDate, String endDate);

    /**
     * 按月份获取销售额统计
     */
    List<SalesDTO> getSalesByMonth();

    /**
     * 获取书籍销量排行榜
     */
    List<SalesRankDTO> getTopSalesBooks(Integer limit);

    /**
     * 获取分类销量排行榜
     */
    List<Map<String, Object>> getTopSalesCategories();

    /**
     * 获取作者销量排行榜
     */
    List<Map<String, Object>> getTopSalesAuthors(Integer limit);

    /**
     * 获取分类销量趋势（按月份）
     */
    List<Map<String, Object>> getCategorySalesTrend();

    /**
     * 获取用户年龄段分布
     */
    List<UserProfileDTO> getUserAgeDistribution();

    /**
     * 获取用户性别分布
     */
    List<UserProfileDTO> getUserGenderDistribution();

    /**
     * 获取用户消费区间分布
     */
    List<UserProfileDTO> getUserSpentDistribution();

    /**
     * 获取用户角色分布（只展示前3个角色）
     */
    List<UserProfileDTO> getUserRoleDistribution();

    /**
     * 获取每日新增用户报表
     */
    List<DailyUserReportDTO> getDailyUserReport();

    /**
     * 获取热门搜索词排行
     */
    List<SearchKeywordDTO> getHotSearchKeywords(Integer limit);

    /**
     * 按日期获取搜索统计
     */
    List<Map<String, Object>> getSearchStatsByDate();

    /**
     * 获取搜索结果分布
     */
    List<Map<String, Object>> getSearchResultDistribution();

    /**
     * 获取搜索时段趋势
     */
    List<Map<String, Object>> getSearchHourlyTrend();

    /**
     * 获取订单状态分布
     */
    List<Map<String, Object>> getOrderStatusDistribution();

    /**
     * 获取支付方式分布
     */
    List<Map<String, Object>> getPayTypeDistribution();

    /**
     * 获取书籍分类统计
     */
    List<Map<String, Object>> getBookCategoryStatistics();

    /**
     * 获取书籍价格区间分布
     */
    List<Map<String, Object>> getBookPriceDistribution();
}
