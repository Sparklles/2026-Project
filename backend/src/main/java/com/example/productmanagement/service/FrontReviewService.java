package com.example.productmanagement.service;

import com.example.productmanagement.dto.ReviewDTO;
import com.example.productmanagement.vo.ReviewVO;
import java.util.List;

public interface FrontReviewService {
    /**
     * 获取某本书的评价列表（支持按星级筛选）
     */
    List<ReviewVO> getBookReviews(Long bookId, Integer rating);

    /**
     * 提交新评价
     */
    void addReview(ReviewDTO dto, Long userId);
}
