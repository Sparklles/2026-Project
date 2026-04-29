package com.example.productmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.productmanagement.dto.ReviewDTO;
import com.example.productmanagement.entity.BookReview;
import com.example.productmanagement.entity.SysUser;
import com.example.productmanagement.mapper.BookReviewMapper;
import com.example.productmanagement.mapper.SysUserMapper;
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
    private final SysUserMapper sysUserMapper;

    @Override
    public List<ReviewVO> getBookReviews(Long bookId, Integer rating) {
        LambdaQueryWrapper<BookReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookReview::getBookId, bookId)
                .eq(BookReview::getStatus, 1); // 仅查询审核通过/正常显示的评价

        // 🌟 如果前端传了星级（大于0），则加入星级过滤条件
        if (rating != null && rating > 0) {
            if (rating == 2) {
                // 当传入 2 时，代表前端选择了“2星以下(差评)”，使用 le (Less than or Equal) 小于等于
                wrapper.le(BookReview::getRating, 2);
            } else {
                // 当传入 3、4、5 时，代表精确匹配某个星级，使用 eq (Equal) 等于
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

            // 新增：映射官方回复内容
            vo.setAdminReply(review.getAdminReply());

            // 查询并脱敏用户名
            SysUser user = sysUserMapper.selectById(review.getUserId());
            if (user != null) {
                vo.setUsername(maskUsername(user.getUsername()));
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
        review.setStatus(1); // 默认直接显示，严谨的系统可设为 0 待管理员审核
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
