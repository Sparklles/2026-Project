package com.example.productmanagement.controller.front;

import com.example.productmanagement.controller.Result;
import com.example.productmanagement.dto.ReviewQueryDTO;
import com.example.productmanagement.dto.ReviewUpdateDTO;
import com.example.productmanagement.service.UserReviewService;
import com.example.productmanagement.vo.ReviewManageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/front/user-reviews")
public class UserReviewController {

    @Autowired
    private UserReviewService userReviewService;

    // 假设当前登录用户的ID为2 (后续替换为Token获取)
    private final Long MOCK_USER_ID = 2L;

    @PostMapping("/list")
    public Result<List<ReviewManageVO>> getReviewList(@RequestBody ReviewQueryDTO queryDTO) {
        List<ReviewManageVO> list = userReviewService.getUserReviewList(MOCK_USER_ID, queryDTO);
        return Result.success(list);
    }

    @PutMapping("/update")
    public Result<?> updateReview(@RequestBody ReviewUpdateDTO dto) {
        userReviewService.updateReview(MOCK_USER_ID, dto);
        return Result.success("评价修改成功");
    }

    @DeleteMapping("/remove/{id}")
    public Result<?> deleteReview(@PathVariable Long id) {
        userReviewService.deleteReview(MOCK_USER_ID, id);
        return Result.success("评价删除成功");
    }
}
