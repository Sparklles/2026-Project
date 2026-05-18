package com.example.productmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.productmanagement.dto.PageQueryDTO;
import com.example.productmanagement.dto.ReviewDTO;
import com.example.productmanagement.entity.BookReview;
import com.example.productmanagement.mapper.BookReviewMapper;
import com.example.productmanagement.mapper.OrderItemMapper;
import com.example.productmanagement.service.BookReviewService;
import com.example.productmanagement.vo.ReviewDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookReviewServiceImpl extends ServiceImpl<BookReviewMapper, BookReview> implements BookReviewService {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitReview(Long userId, ReviewDTO dto) {
        // 1. 底层拦截校验：防刷单、防恶意差评机制
        int purchasedCount = orderItemMapper.countPurchasedRecord(userId, dto.getBookId());
        if (purchasedCount <= 0) {
            throw new RuntimeException("风控拦截：抱歉，您尚未购买该航海书籍，无法发表评价。");
        }

        // 2. 持久化用户生成内容 (UGC)
        BookReview review = new BookReview();
        review.setUserId(userId);
        review.setBookId(dto.getBookId());
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setStatus(1); // 默认对外展示正常
        this.save(review);

        // 3. (扩展) 实际业务中，这里通常会发送一个MQ消息或异步事件，
        // 让系统去重新计算并更新 book_info 表中的平均评分 (average_rating) 和总评价数。
    }

    @Override
    public IPage<BookReview> getFrontReviews(Long bookId, PageQueryDTO queryDTO) {
        // 前台只展示 status = 1 的合规评价，并按时间倒序
        Page<BookReview> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        LambdaQueryWrapper<BookReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookReview::getBookId, bookId)
                .eq(BookReview::getStatus, 1)
                .orderByDesc(BookReview::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public IPage<ReviewDetailVO> getAdminReviewList(Integer rating, Integer status, PageQueryDTO queryDTO) {
        // 使用 MyBatis-Plus 提供的分页对象包裹我们在 XML 中手写的复杂查询
        Page<ReviewDetailVO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());

        // 调用我们之前在 BookReviewMapper 中定义的多表联查方法
        // MyBatis-Plus 拦截器会自动拦截该方法并执行 LIMIT 分页拼接
        page.setRecords(this.baseMapper.selectReviewListForAdmin(rating, status));

        return page;
    }

    @Override
    public boolean hideReview(Long reviewId) {
        // 更新 status = 0，实现违规内容的即时隐藏
        BookReview review = new BookReview();
        review.setId(reviewId);
        review.setStatus(0);
        return this.updateById(review);
    }

    @Override
    public boolean replyReview(Long reviewId, String replyContent) {
        if (replyContent == null || replyContent.trim().isEmpty()) {
            throw new IllegalArgumentException("回复内容不能为空");
        }

        BookReview review = new BookReview();
        review.setId(reviewId);
        review.setAdminReply(replyContent); // 写入官方回复
        return this.updateById(review);
    }
}
