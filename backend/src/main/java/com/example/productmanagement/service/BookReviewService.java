package com.example.productmanagement.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.productmanagement.dto.PageQueryDTO;
import com.example.productmanagement.dto.ReviewDTO;
import com.example.productmanagement.entity.BookReview;
import com.example.productmanagement.vo.ReviewDetailVO;


public interface BookReviewService extends IService<BookReview> {

    // ================= 前台互动功能 =================

    /**
     * (1) 提交书籍评分与评论 (包含购买资格校验)
     */
    void submitReview(Long userId, ReviewDTO dto);

    /**
     * (1) 查看书籍评分和评论 (前台分页查看，仅查询正常状态的评价)
     */
    IPage<BookReview> getFrontReviews(Long bookId, PageQueryDTO queryDTO);

    // ================= 后台内容管理 =================

    /**
     * (2) 后台多维度筛选评价列表 (带有用户名和书名)
     */
    IPage<ReviewDetailVO> getAdminReviewList(Integer rating, Integer status, PageQueryDTO queryDTO);

    /**
     * (2) 隐藏违规评论
     */
    boolean hideReview(Long reviewId);

    /**
     * (2) 官方管理员回复评论
     */
    boolean replyReview(Long reviewId, String replyContent);
}
