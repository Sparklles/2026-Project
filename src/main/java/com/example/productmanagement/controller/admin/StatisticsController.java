package com.example.productmanagement.controller.admin;

import com.example.productmanagement.controller.Result;
import com.example.productmanagement.dto.*;
import com.example.productmanagement.service.StatisticsService;

import com.example.productmanagement.utils.ExcelExportUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * 统计报表控制器
 */
@RestController
@RequestMapping("/api/statistics")
//@CrossOrigin(origins = "*")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取销售汇总
     */
    @GetMapping("/sales/summary")
    public Result<SalesSummaryDTO> getSalesSummary() {
        SalesSummaryDTO summary = statisticsService.getSalesSummary();
        return Result.success(summary);
    }

    /**
     * 按日期获取销售额统计（支持日期范围筛选）
     */
    @GetMapping("/sales/by-date")
    public Result<List<SalesDTO>> getSalesByDate(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<SalesDTO> salesList;
        if ((startDate != null && !startDate.isEmpty()) || (endDate != null && !endDate.isEmpty())) {
            salesList = statisticsService.getSalesByDateRange(startDate, endDate);
        } else {
            salesList = statisticsService.getSalesByDate();
        }
        return Result.success(salesList);
    }

    /**
     * 按日期范围获取销售汇总
     */
    @GetMapping("/sales/summary-by-date")
    public Result<SalesSummaryDTO> getSalesSummaryByDateRange(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        SalesSummaryDTO summary = statisticsService.getSalesSummaryByDateRange(startDate, endDate);
        return Result.success(summary);
    }

    /**
     * 按月份获取销售额统计
     */
    @GetMapping("/sales/by-month")
    public Result<List<SalesDTO>> getSalesByMonth() {
        List<SalesDTO> salesList = statisticsService.getSalesByMonth();
        return Result.success(salesList);
    }

    /**
     * 获取书籍销量排行榜
     */
    @GetMapping("/sales/top-books")
    public Result<List<SalesRankDTO>> getTopSalesBooks(@RequestParam(defaultValue = "10") Integer limit) {
        List<SalesRankDTO> ranking = statisticsService.getTopSalesBooks(limit);
        return Result.success(ranking);
    }

    /**
     * 获取分类销量排行榜
     */
    @GetMapping("/sales/top-categories")
    public Result<List<Map<String, Object>>> getTopSalesCategories() {
        List<Map<String, Object>> ranking = statisticsService.getTopSalesCategories();
        return Result.success(ranking);
    }

    /**
     * 获取作者销量排行榜
     */
    @GetMapping("/sales/top-authors")
    public Result<List<Map<String, Object>>> getTopSalesAuthors(@RequestParam(defaultValue = "10") Integer limit) {
        List<Map<String, Object>> ranking = statisticsService.getTopSalesAuthors(limit);
        return Result.success(ranking);
    }

    /**
     * 获取用户年龄段分布
     */
    @GetMapping("/user/age-distribution")
    public Result<List<UserProfileDTO>> getUserAgeDistribution() {
        List<UserProfileDTO> distribution = statisticsService.getUserAgeDistribution();
        return Result.success(distribution);
    }

    /**
     * 获取用户性别分布
     */
    @GetMapping("/user/gender-distribution")
    public Result<List<UserProfileDTO>> getUserGenderDistribution() {
        List<UserProfileDTO> distribution = statisticsService.getUserGenderDistribution();
        return Result.success(distribution);
    }

    /**
     * 获取用户消费区间分布
     */
    @GetMapping("/user/spent-distribution")
    public Result<List<UserProfileDTO>> getUserSpentDistribution() {
        List<UserProfileDTO> distribution = statisticsService.getUserSpentDistribution();
        return Result.success(distribution);
    }

    /**
     * 获取用户角色分布（只展示前3个角色：初级海员、经验丰富海员、非海员买家）
     */
    @GetMapping("/user/role-distribution")
    public Result<List<UserProfileDTO>> getUserRoleDistribution() {
        List<UserProfileDTO> distribution = statisticsService.getUserRoleDistribution();
        return Result.success(distribution);
    }

    /**
     * 获取每日新增用户报表（含环比、同比）
     */
    @GetMapping("/user/daily-report")
    public Result<List<DailyUserReportDTO>> getDailyUserReport() {
        List<DailyUserReportDTO> report = statisticsService.getDailyUserReport();
        return Result.success(report);
    }

    /**
     * 获取热门搜索词排行
     */
    @GetMapping("/search/hot-keywords")
    public Result<List<SearchKeywordDTO>> getHotSearchKeywords(@RequestParam(defaultValue = "10") Integer limit) {
        List<SearchKeywordDTO> keywords = statisticsService.getHotSearchKeywords(limit);
        return Result.success(keywords);
    }

    /**
     * 按日期获取搜索统计
     */
    @GetMapping("/search/by-date")
    public Result<List<Map<String, Object>>> getSearchStatsByDate() {
        List<Map<String, Object>> stats = statisticsService.getSearchStatsByDate();
        return Result.success(stats);
    }

    /**
     * 获取搜索结果分布
     */
    @GetMapping("/search/result-distribution")
    public Result<List<Map<String, Object>>> getSearchResultDistribution() {
        List<Map<String, Object>> distribution = statisticsService.getSearchResultDistribution();
        return Result.success(distribution);
    }

    /**
     * 获取搜索时段趋势
     */
    @GetMapping("/search/hourly-trend")
    public Result<List<Map<String, Object>>> getSearchHourlyTrend() {
        List<Map<String, Object>> trend = statisticsService.getSearchHourlyTrend();
        return Result.success(trend);
    }

    /**
     * 获取订单状态分布
     */
    @GetMapping("/order/status-distribution")
    public Result<List<Map<String, Object>>> getOrderStatusDistribution() {
        List<Map<String, Object>> distribution = statisticsService.getOrderStatusDistribution();
        return Result.success(distribution);
    }

    /**
     * 获取支付方式分布
     */
    @GetMapping("/order/pay-type-distribution")
    public Result<List<Map<String, Object>>> getPayTypeDistribution() {
        List<Map<String, Object>> distribution = statisticsService.getPayTypeDistribution();
        return Result.success(distribution);
    }

    /**
     * 获取分类销量趋势（按月）
     */
    @GetMapping("/sales/category-trend")
    public Result<List<Map<String, Object>>> getCategorySalesTrend() {
        List<Map<String, Object>> trend = statisticsService.getCategorySalesTrend();
        return Result.success(trend);
    }

    /**
     * 获取书籍分类统计
     */
    @GetMapping("/book/category-statistics")
    public Result<List<Map<String, Object>>> getBookCategoryStatistics() {
        List<Map<String, Object>> statistics = statisticsService.getBookCategoryStatistics();
        return Result.success(statistics);
    }

    /**
     * 获取书籍价格区间分布
     */
    @GetMapping("/book/price-distribution")
    public Result<List<Map<String, Object>>> getBookPriceDistribution() {
        List<Map<String, Object>> distribution = statisticsService.getBookPriceDistribution();
        return Result.success(distribution);
    }

    /**
     * 导出书籍销量排行数据为Excel
     */
    @GetMapping("/export/top-books")
    public void exportTopBooks(@RequestParam(defaultValue = "10") Integer limit,
                               HttpServletResponse response) throws Exception {
        List<SalesRankDTO> data = statisticsService.getTopSalesBooks(limit);
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("书籍销量排行", "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        ExcelExportUtil.exportSalesRank(data, response.getOutputStream());
    }

    /**
     * 导出分类销量排行数据为Excel
     */
    @GetMapping("/export/top-categories")
    public void exportTopCategories(HttpServletResponse response) throws Exception {
        List<Map<String, Object>> data = statisticsService.getTopSalesCategories();
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("分类销量排行", "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        ExcelExportUtil.exportCategoryRank(data, response.getOutputStream());
    }

    /**
     * 导出分类销量趋势数据为Excel
     */
    @GetMapping("/export/category-trend")
    public void exportCategoryTrend(HttpServletResponse response) throws Exception {
        List<Map<String, Object>> data = statisticsService.getCategorySalesTrend();
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("分类销量趋势", "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        ExcelExportUtil.exportCategoryTrend(data, response.getOutputStream());
    }

    /**
     * 导出销售数据为Excel
     */
    @GetMapping("/export/sales-data")
    public void exportSalesData(HttpServletResponse response) throws Exception {
        List<SalesDTO> data = statisticsService.getSalesByDate();

        // 设置响应头
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("销售数据统计", "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        // 导出Excel
        ExcelExportUtil.exportSalesData(data, response.getOutputStream());
    }

    /**
     * 导出用户画像数据为Excel
     */
    @GetMapping("/export/user-profile")
    public void exportUserProfile(@RequestParam(required = false) String type,
                                   HttpServletResponse response) throws Exception {
        List<UserProfileDTO> data;

        if (type == null) {
            type = "age";
        }
        switch (type) {
            case "age":
                data = statisticsService.getUserAgeDistribution();
                break;
            case "gender":
                data = statisticsService.getUserGenderDistribution();
                break;
            case "spent":
                data = statisticsService.getUserSpentDistribution();
                break;
            default:
                data = statisticsService.getUserAgeDistribution();
        }

        // 设置响应头
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("用户画像统计", "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        // 导出Excel
        ExcelExportUtil.exportUserProfile(data, response.getOutputStream());
    }

    /**
     * 导出搜索关键词数据为Excel
     */
    @GetMapping("/export/search-keywords")
    public void exportSearchKeywords(@RequestParam(defaultValue = "10") Integer limit,
                                      HttpServletResponse response) throws Exception {
        List<SearchKeywordDTO> data = statisticsService.getHotSearchKeywords(limit);

        // 设置响应头
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("热门搜索关键词", "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        // 导出Excel
        ExcelExportUtil.exportSearchKeywords(data, response.getOutputStream());
    }
}
