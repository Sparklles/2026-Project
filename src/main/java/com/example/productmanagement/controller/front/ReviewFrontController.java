package com.example.productmanagement.controller.front;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.example.productmanagement.controller.Result;
import com.example.productmanagement.dto.PageQueryDTO;
import com.example.productmanagement.dto.ReviewDTO;
import com.example.productmanagement.entity.BookReview;
import com.example.productmanagement.service.BookReviewService;
import com.example.productmanagement.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 前台-商品评价互动控制器
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewFrontController {

    @Autowired
    private BookReviewService bookReviewService;

    /**
     * (1) 提交书籍评分与评论
     */
    @PostMapping("/submit")
    public Result<?> submitReview(@RequestBody ReviewDTO dto) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error(401, "\u8bf7\u5148\u767b\u5f55");
        }
        if (dto.getBookId() == null || dto.getRating() == null) {
            return Result.error(400, "书籍ID和评分不能为空");
        }
        bookReviewService.submitReview(userId, dto);
        return Result.success("感谢您的评价！");
    }

    /**
     * (1) 查看书籍评分和评论 (分页列表)
     * 允许游客查看，无需 userId
     */
    @GetMapping("/book/{bookId}")
    public Result<IPage<BookReview>> getBookReviews(@PathVariable("bookId") Long bookId, PageQueryDTO queryDTO) {
        IPage<BookReview> pageResult = bookReviewService.getFrontReviews(bookId, queryDTO);
        return Result.success(pageResult);
    }
}

