package com.example.productmanagement.controller.front;

import com.example.productmanagement.controller.Result; // 替换为你的Result类路径
import com.example.productmanagement.dto.ReviewDTO;
import com.example.productmanagement.service.FrontReviewService;
import com.example.productmanagement.utils.UserHolder;
import com.example.productmanagement.vo.ReviewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/front/reviews")
public class FrontReviewController {

    @Autowired
    private FrontReviewService frontReviewService;

    /**
     * 1. 动态获取评价列表（支持按星级过滤）
     */
    @GetMapping("/book/{bookId}")
    public Result<List<ReviewVO>> getBookReviews(
            @PathVariable("bookId") Long bookId,
            @RequestParam(value = "rating", required = false, defaultValue = "0") Integer rating) {

        List<ReviewVO> reviews = frontReviewService.getBookReviews(bookId, rating);
        return Result.success(reviews);
    }

    /**
     * 2. 提交评价
     */
    @PostMapping
    public Result<?> submitReview(@RequestBody ReviewDTO dto) {
        if (dto.getRating() == null || dto.getRating() < 1) {
            return Result.error(400, "请至少给出1星评价");
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            return Result.error(400, "评价内容不能为空");
        }

        Long currentUserId = UserHolder.getUserId();
        if (currentUserId == null) {
            return Result.error(401, "\u8bf7\u5148\u767b\u5f55");
        }

        frontReviewService.addReview(dto, currentUserId);
        return Result.success("评价发布成功！");
    }
}

