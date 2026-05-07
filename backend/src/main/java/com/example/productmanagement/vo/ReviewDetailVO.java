package com.example.productmanagement.vo;

import lombok.Data;
import java.util.Date;

/**
 * 后台评价管控列表视图对象 (VO)
 * 聚合了用户信息和书籍信息，方便管理员一目了然地进行风控
 */
@Data
public class ReviewDetailVO {

    /** 评价主键ID */
    private Long id;

    /** 评分 (1-5星) */
    private Integer rating;

    /** 评价文字内容 */
    private String content;

    /** 当前状态: 1-正常显示, 0-管理员屏蔽隐藏 */
    private Integer status;

    /** 管理员官方回复内容 */
    private String adminReply;

    /** 评价发布时间 */
    private Date createTime;

    // ==========================================
    // 下面是多表 JOIN 查询带出的关联表额外字段
    // ==========================================

    /** * 评价人用户名 (来源于 sys_user 表的 username)
     * 前端可直接展示为: "用户 [海员张三] 评价了..."
     */
    private String reviewerName;

    /** * 被评价的书名 (来源于 book_info 表的 title)
     */
    private String bookTitle;
}
