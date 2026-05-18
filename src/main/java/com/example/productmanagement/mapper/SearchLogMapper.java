package com.example.productmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.productmanagement.entity.SearchLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 搜索日志数据访问层
 */
@Mapper
public interface SearchLogMapper extends BaseMapper<SearchLog> {

    /**
     * 获取热门搜索词排行
     */
    @Select("SELECT " +
            "  search_keyword, " +
            "  COUNT(*) as search_count, " +
            "  SUM(result_count) as total_results " +
            "FROM search_log " +
            "WHERE deleted = 0 " +
            "GROUP BY search_keyword " +
            "ORDER BY search_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getHotSearchKeywords(@Param("limit") Integer limit);

    /**
     * 按日期统计搜索次数
     */
    @Select("SELECT " +
            "  DATE(create_time) as stat_date, " +
            "  COUNT(*) as search_count, " +
            "  COUNT(DISTINCT user_id) as unique_users " +
            "FROM search_log " +
            "WHERE deleted = 0 " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY stat_date")
    List<Map<String, Object>> getSearchStatsByDate();

    /**
     * 统计搜索结果命中情况
     */
    @Select("SELECT " +
            "  CASE " +
            "    WHEN result_count = 0 THEN '无结果' " +
            "    WHEN result_count BETWEEN 1 AND 10 THEN '1-10条' " +
            "    WHEN result_count BETWEEN 11 AND 50 THEN '11-50条' " +
            "    WHEN result_count BETWEEN 51 AND 100 THEN '51-100条' " +
            "    ELSE '100条以上' " +
            "  END as result_range, " +
            "  COUNT(*) as search_count " +
            "FROM search_log " +
            "WHERE deleted = 0 " +
            "GROUP BY result_range " +
            "ORDER BY MIN(result_count)")
    List<Map<String, Object>> getSearchResultDistribution();

    /**
     * 搜索趋势统计
     */
    @Select("SELECT " +
            "  DATE_FORMAT(create_time, '%H') as hour, " +
            "  COUNT(*) as search_count " +
            "FROM search_log " +
            "WHERE deleted = 0 " +
            "GROUP BY DATE_FORMAT(create_time, '%H') " +
            "ORDER BY hour")
    List<Map<String, Object>> getSearchHourlyTrend();

    /**
     * 获取热门搜索词（只返回关键词列表）
     */
    @Select("SELECT search_keyword " +
            "FROM search_log " +
            "WHERE deleted = 0 " +
            "GROUP BY search_keyword " +
            "ORDER BY COUNT(*) DESC " +
            "LIMIT #{limit}")
    List<String> getHotKeywords(@Param("limit") Integer limit);
}
