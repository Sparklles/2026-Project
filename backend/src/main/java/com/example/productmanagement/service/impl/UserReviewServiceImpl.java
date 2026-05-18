package com.example.productmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.productmanagement.dto.ReviewQueryDTO;
import com.example.productmanagement.dto.ReviewUpdateDTO;
import com.example.productmanagement.entity.BookInfo;
import com.example.productmanagement.entity.BookReview;
import com.example.productmanagement.mapper.BookInfoMapper;
import com.example.productmanagement.mapper.BookReviewMapper;
import com.example.productmanagement.service.UserReviewService;
import com.example.productmanagement.vo.ProductDetailVO;
import com.example.productmanagement.vo.ReviewManageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserReviewServiceImpl implements UserReviewService {

    private final BookReviewMapper bookReviewMapper;
    private final BookInfoMapper bookInfoMapper;

    @Override
    public List<ReviewManageVO> getUserReviewList(Long userId, ReviewQueryDTO query) {

        // 💡 提示：对于 B2C 商城，“来自卖家的评价 (received)” 通常是系统自动好评或没有此功能。
        // 如果前端传了 received，我们可以返回模拟数据，或者查特定表。这里我们重点实现 given（给他人的评价）
        if ("received".equals(query.getTabType())) {
            return getMockReceivedReviews();
        }

        // ====== 处理 given (我作出的评价) ======
        LambdaQueryWrapper<BookReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookReview::getUserId, userId);

        // 1. 过滤好中差评
        if ("good".equals(query.getFilterType())) {
            wrapper.eq(BookReview::getRating, 5); // 5星是好评
        } else if ("neutral".equals(query.getFilterType())) {
            wrapper.in(BookReview::getRating, 3, 4); // 3-4星是中评
        } else if ("bad".equals(query.getFilterType())) {
            wrapper.in(BookReview::getRating, 1, 2); // 1-2星是差评
        }

        // 2. 过滤有无内容
        if ("hasContent".equals(query.getFilterContent())) {
            wrapper.isNotNull(BookReview::getContent).ne(BookReview::getContent, "");
        } else if ("noContent".equals(query.getFilterContent())) {
            wrapper.and(w -> w.isNull(BookReview::getContent).or().eq(BookReview::getContent, ""));
        }

        wrapper.orderByDesc(BookReview::getCreateTime);
        List<BookReview> reviews = bookReviewMapper.selectList(wrapper);

        // 3. 数据映射与组装
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        List<ReviewManageVO> resultList = reviews.stream().map(review -> {
            ReviewManageVO vo = new ReviewManageVO();
            vo.setId(review.getId());
            vo.setTabType("given");

            // 星级转化
            if (review.getRating() == 5) vo.setRatingType("good");
            else if (review.getRating() >= 3) vo.setRatingType("neutral");
            else vo.setRatingType("bad");

            // 如果内容为空，标记为系统默认
            vo.setIsSystem(!StringUtils.hasText(review.getContent()));
            vo.setContent(review.getContent());
            vo.setDate(sdf.format(review.getCreateTime()));
            vo.setAdminReply(review.getAdminReply());
            vo.setTargetName("航海时代自营商城"); // 假设均为自营

            // 查出商品信息
            BookInfo book = bookInfoMapper.selectById(review.getBookId());
            if (book != null) {
                ProductDetailVO p = new ProductDetailVO();
                p.setId(book.getId());
                p.setTitle(book.getTitle());
                p.setMinPrice(book.getPrice());
                vo.setProduct(p);
            }
            return vo;
        }).collect(Collectors.toList());

        // 4. 关键字搜索过滤 (基于商品名或评价内容)
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().toLowerCase();
            resultList = resultList.stream().filter(vo ->
                    (vo.getContent() != null && vo.getContent().toLowerCase().contains(kw)) ||
                            (vo.getProduct() != null && vo.getProduct().getTitle() != null && vo.getProduct().getTitle().toLowerCase().contains(kw))
            ).collect(Collectors.toList());
        }

        return resultList;
    }

    /**
     * 辅助方法：生成模拟的“来自卖家的评价”
     */
    private List<ReviewManageVO> getMockReceivedReviews() {
        List<ReviewManageVO> list = new ArrayList<>();
        ReviewManageVO vo1 = new ReviewManageVO();
        vo1.setId(999L);
        vo1.setTabType("received");
        vo1.setRatingType("good");
        vo1.setIsSystem(true);
        vo1.setContent("评价方未及时做出评价，系统默认好评！");
        vo1.setDate("2026-04-20 08:14:03");
        vo1.setTargetName("航海时代官方直营店");
        ProductDetailVO p1 = new ProductDetailVO();
        p1.setId(101L); p1.setTitle("适用远洋船长考核 高级海图作业专用绘图仪套装..."); p1.setMinPrice(new java.math.BigDecimal("128.00"));
        vo1.setProduct(p1);
        list.add(vo1);
        return list;
    }

    @Override
    public void updateReview(Long userId, ReviewUpdateDTO dto) {
        BookReview review = bookReviewMapper.selectById(dto.getId());
        if (review == null) {
            throw new RuntimeException("评价不存在");
        }
        // 安全校验：只允许修改本人的评价
        if (!review.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此评价");
        }

        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        // 如果用户修改了评价，可以选择清空商家的历史回复
        // review.setAdminReply(null);

        bookReviewMapper.updateById(review);
    }

    @Override
    public void deleteReview(Long userId, Long reviewId) {
        BookReview review = bookReviewMapper.selectById(reviewId);
        if (review != null && review.getUserId().equals(userId)) {
            bookReviewMapper.deleteById(reviewId);
        } else if (review != null) {
            throw new RuntimeException("无权删除此评价");
        }
    }
}
