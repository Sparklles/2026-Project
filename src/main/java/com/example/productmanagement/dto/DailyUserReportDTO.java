package com.example.productmanagement.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 每日新增用户报表DTO
 */
@Data
public class DailyUserReportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 统计日期
     */
    private String statDate;

    /**
     * 新增用户数
     */
    private Long newUsers;

    /**
     * 累计用户数
     */
    private Long cumulativeUsers;

    /**
     * 环比增长率（与前一天相比）
     */
    private String momRate;

    /**
     * 同比增长率（与去年同天相比）
     */
    private String yoyRate;
}
