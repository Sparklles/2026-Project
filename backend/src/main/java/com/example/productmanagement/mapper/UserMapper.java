package com.example.productmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.example.productmanagement.dto.UserSearchDto;
import com.example.productmanagement.entity.User;
import com.example.productmanagement.vo.AdminUserListVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author freedom
 * @description 针对表【user(系统用户核心表(鉴权与风控))】的数据库操作Mapper
 * @createDate 2026-04-21 15:14:41
 * @Entity com.dujiang.sailmart.entity.User
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 后台用户列表分页查询（LEFT JOIN user_detail，支持三维度模糊搜索）。
     *
     * @param page   MyBatis-Plus 分页对象
     * @param params 搜索条件（loginAccount / email / phone，任意组合）
     * @return 分页结果
     */
    IPage<AdminUserListVo> selectAdminUserPage(
            @Param("page") IPage<AdminUserListVo> page,
            @Param("params") UserSearchDto params);

    @Select("SELECT COUNT(*) FROM `user` WHERE is_deleted = 0")
    Long countActiveUsers();

    @Select("SELECT age_range, COUNT(*) AS user_count " +
            "FROM ( " +
            "  SELECT " +
            "    CASE " +
            "      WHEN ud.birthday IS NULL THEN '未知' " +
            "      WHEN TIMESTAMPDIFF(YEAR, ud.birthday, CURDATE()) < 18 THEN '18岁以下' " +
            "      WHEN TIMESTAMPDIFF(YEAR, ud.birthday, CURDATE()) BETWEEN 18 AND 25 THEN '18-25岁' " +
            "      WHEN TIMESTAMPDIFF(YEAR, ud.birthday, CURDATE()) BETWEEN 26 AND 35 THEN '26-35岁' " +
            "      WHEN TIMESTAMPDIFF(YEAR, ud.birthday, CURDATE()) BETWEEN 36 AND 45 THEN '36-45岁' " +
            "      WHEN TIMESTAMPDIFF(YEAR, ud.birthday, CURDATE()) BETWEEN 46 AND 55 THEN '46-55岁' " +
            "      ELSE '55岁以上' " +
            "    END AS age_range, " +
            "    CASE " +
            "      WHEN ud.birthday IS NULL THEN 99 " +
            "      WHEN TIMESTAMPDIFF(YEAR, ud.birthday, CURDATE()) < 18 THEN 1 " +
            "      WHEN TIMESTAMPDIFF(YEAR, ud.birthday, CURDATE()) BETWEEN 18 AND 25 THEN 2 " +
            "      WHEN TIMESTAMPDIFF(YEAR, ud.birthday, CURDATE()) BETWEEN 26 AND 35 THEN 3 " +
            "      WHEN TIMESTAMPDIFF(YEAR, ud.birthday, CURDATE()) BETWEEN 36 AND 45 THEN 4 " +
            "      WHEN TIMESTAMPDIFF(YEAR, ud.birthday, CURDATE()) BETWEEN 46 AND 55 THEN 5 " +
            "      ELSE 6 " +
            "    END AS sort_order " +
            "  FROM `user` u " +
            "  LEFT JOIN user_detail ud ON u.id = ud.user_id " +
            "  WHERE u.is_deleted = 0 " +
            ") t " +
            "GROUP BY age_range, sort_order " +
            "ORDER BY sort_order")
    List<Map<String, Object>> countUsersByAgeRange();

    @Select("SELECT " +
            "  CASE ud.gender " +
            "    WHEN 1 THEN '男' " +
            "    WHEN 2 THEN '女' " +
            "    ELSE '未知' " +
            "  END AS gender_name, " +
            "  COUNT(*) AS user_count " +
            "FROM `user` u " +
            "LEFT JOIN user_detail ud ON u.id = ud.user_id " +
            "WHERE u.is_deleted = 0 " +
            "GROUP BY CASE ud.gender WHEN 1 THEN '男' WHEN 2 THEN '女' ELSE '未知' END " +
            "ORDER BY MIN(COALESCE(ud.gender, 0))")
    List<Map<String, Object>> countUsersByGender();

    @Select("SELECT spent_range, COUNT(*) AS user_count " +
            "FROM ( " +
            "  SELECT " +
            "    CASE " +
            "      WHEN COALESCE(s.total_spent, 0) < 100 THEN '0-100元' " +
            "      WHEN COALESCE(s.total_spent, 0) BETWEEN 100 AND 500 THEN '100-500元' " +
            "      WHEN COALESCE(s.total_spent, 0) BETWEEN 501 AND 1000 THEN '500-1000元' " +
            "      WHEN COALESCE(s.total_spent, 0) BETWEEN 1001 AND 3000 THEN '1000-3000元' " +
            "      ELSE '3000元以上' " +
            "    END AS spent_range, " +
            "    CASE " +
            "      WHEN COALESCE(s.total_spent, 0) < 100 THEN 1 " +
            "      WHEN COALESCE(s.total_spent, 0) BETWEEN 100 AND 500 THEN 2 " +
            "      WHEN COALESCE(s.total_spent, 0) BETWEEN 501 AND 1000 THEN 3 " +
            "      WHEN COALESCE(s.total_spent, 0) BETWEEN 1001 AND 3000 THEN 4 " +
            "      ELSE 5 " +
            "    END AS sort_order " +
            "  FROM `user` u " +
            "  LEFT JOIN ( " +
            "    SELECT user_id, SUM(pay_amount) AS total_spent " +
            "    FROM `order` " +
            "    WHERE deleted = 0 AND order_status = 4 AND pay_status = 2 " +
            "    GROUP BY user_id " +
            "  ) s ON u.id = s.user_id " +
            "  WHERE u.is_deleted = 0 " +
            ") t " +
            "GROUP BY spent_range, sort_order " +
            "ORDER BY sort_order")
    List<Map<String, Object>> countUsersBySpentRange();

    @Select("SELECT " +
            "  CASE role " +
            "    WHEN 1 THEN '普通用户' " +
            "    WHEN 2 THEN '管理员' " +
            "    ELSE '其他' " +
            "  END AS role_name, " +
            "  COUNT(*) AS user_count " +
            "FROM `user` " +
            "WHERE is_deleted = 0 " +
            "GROUP BY role " +
            "ORDER BY role")
    List<Map<String, Object>> countUsersByRole();

    @Select("SELECT DATE(create_time) AS stat_date, COUNT(*) AS new_users " +
            "FROM `user` " +
            "WHERE is_deleted = 0 " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY stat_date")
    List<Map<String, Object>> getDailyNewUsers();
}




