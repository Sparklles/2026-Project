package com.example.productmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.productmanagement.dto.ReviewDTO;
import com.example.productmanagement.entity.BookReview;
// 🌟 1. 核心修复：引入你系统真正的 User 实体和 Mapper
import com.example.productmanagement.entity.User;
import com.example.productmanagement.mapper.BookReviewMapper;
import com.example.productmanagement.mapper.UserMapper;
import com.example.productmanagement.service.FrontReviewService;
import com.example.productmanagement.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FrontReviewServiceImpl implements FrontReviewService {

    private final BookReviewMapper bookReviewMapper;
    // 🌟 2. 核心修复：注入你系统真正的 UserMapper，而不是 SysUserMapper
    private final UserMapper userMapper;

    @Override
    public List<ReviewVO> getBookReviews(Long bookId, Integer rating) {
        LambdaQueryWrapper<BookReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookReview::getBookId, bookId)
                .eq(BookReview::getStatus, 1);

        if (rating != null && rating > 0) {
            if (rating == 2) {
                wrapper.le(BookReview::getRating, 2);
            } else {
                wrapper.eq(BookReview::getRating, rating);
            }
        }

        wrapper.orderByDesc(BookReview::getCreateTime);

        List<BookReview> reviewList = bookReviewMapper.selectList(wrapper);

        return reviewList.stream().map(review -> {
            ReviewVO vo = new ReviewVO();
            vo.setId(review.getId());
            vo.setRating(review.getRating());
            vo.setContent(review.getContent());
            vo.setDate(formatDate(review.getCreateTime()));
            vo.setAdminReply(review.getAdminReply());

            // 🌟 3. 核心修复：使用真正的 User 实体去查询
            User user = userMapper.selectById(review.getUserId());
            if (user != null) {
                // 使用 getLoginAccount() 作为要脱敏的用户名（根据你之前订单代码的逻辑）
                vo.setUsername(maskUsername(user.getLoginAccount()));
                vo.setAvatar("https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png");
            } else {
                vo.setUsername("匿名水手");
                vo.setAvatar("https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png");
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void addReview(ReviewDTO dto, Long userId) {
        BookReview review = new BookReview();
        review.setBookId(dto.getBookId());
        review.setUserId(userId);
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setStatus(1);
        review.setCreateTime(new Date());

        bookReviewMapper.insert(review);
    }

    private String maskUsername(String username) {
        if (username == null || username.length() <= 1) return username;
        if (username.length() == 2) return username.charAt(0) + "*";
        return username.charAt(0) + "**" + username.charAt(username.length() - 1);
    }

    private String formatDate(Date date) {
        return date == null ? "" : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }
}