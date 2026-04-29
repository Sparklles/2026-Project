package com.example.productmanagement.service;

import com.example.productmanagement.dto.ReviewQueryDTO;
import com.example.productmanagement.dto.ReviewUpdateDTO;
import com.example.productmanagement.vo.ReviewManageVO;
import java.util.List;

public interface UserReviewService {
    /** 获取用户的评价管理列表 */
    List<ReviewManageVO> getUserReviewList(Long userId, ReviewQueryDTO queryDTO);
    void updateReview(Long userId, ReviewUpdateDTO dto);
    void deleteReview(Long userId, Long reviewId);
}
