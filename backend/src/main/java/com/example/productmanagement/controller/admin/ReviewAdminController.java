package com.example.productmanagement.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.example.productmanagement.controller.Result;
import com.example.productmanagement.dto.PageQueryDTO;
import com.example.productmanagement.service.BookReviewService;
import com.example.productmanagement.vo.ReviewDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 后台-评价内容风控管控控制器
 */
@RestController
@RequestMapping("/api/admin/reviews")
public class ReviewAdminController {

    @Autowired
    private BookReviewService bookReviewService;

    /**
     * (2) 后台多维度筛选评价列表 (包含用户名和被评书名)
     * 支持传入 rating(如1星) 或 status(如已隐藏) 作为检索条件
     */
    @GetMapping
    public Result<IPage<ReviewDetailVO>> getAdminReviewList(
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Integer status,
            PageQueryDTO queryDTO) {

        IPage<ReviewDetailVO> pageResult = bookReviewService.getAdminReviewList(rating, status, queryDTO);
        return Result.success(pageResult);
    }

    /**
     * (2) 隐藏违规评论
     */
    @PutMapping("/{id}/hide")
    public Result<?> hideReview(@PathVariable("id") Long id) {
        boolean success = bookReviewService.hideReview(id);
        return success ? Result.success("该条评价已成功隐藏，前台将不再展示") : Result.error(500, "隐藏操作失败");
    }

    /**
     * (2) 官方管理员回复评论
     */
    @PostMapping("/{id}/reply")
    public Result<?> replyReview(@PathVariable("id") Long id, @RequestParam("replyContent") String replyContent) {
        if (replyContent == null || replyContent.trim().isEmpty()) {
            return Result.error(400, "回复内容不能为空");
        }
        boolean success = bookReviewService.replyReview(id, replyContent);
        return success ? Result.success("官方回复成功") : Result.error(500, "回复操作失败");
    }
}
