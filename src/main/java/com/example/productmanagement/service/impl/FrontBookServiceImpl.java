package com.example.productmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.productmanagement.entity.BookImage;
import com.example.productmanagement.entity.BookInfo;
import com.example.productmanagement.entity.BookReview;
import com.example.productmanagement.entity.SysUser;
import com.example.productmanagement.mapper.*;
import com.example.productmanagement.service.FrontBookService;
import com.example.productmanagement.vo.ProductDetailVO;
import com.example.productmanagement.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FrontBookServiceImpl implements FrontBookService {

    private final BookInfoMapper bookInfoMapper;
    private final BookTagMapper bookTagMapper;
    private final BookReviewMapper bookReviewMapper;
    private final SysUserMapper sysUserMapper; // 用于查评价的用户名
    private final BookImageMapper bookImageMapper;

    @Override
    public ProductDetailVO getProductDetail(Long productId) {
        // 1. 查询商品基础信息
        BookInfo bookInfo = bookInfoMapper.selectById(productId);
        // 如果查不到或者已被逻辑删除(is_deleted=1)或者已下架(status=0)，抛出异常
        if (bookInfo == null || bookInfo.getStatus() == 0) {
            throw new RuntimeException("商品不存在或已下架");
        }

        ProductDetailVO detailVO = new ProductDetailVO();
        detailVO.setId(bookInfo.getId());
        detailVO.setTitle(bookInfo.getTitle());
        detailVO.setSubtitle(bookInfo.getDescription());
        detailVO.setMinPrice(bookInfo.getPrice());
        detailVO.setStock(bookInfo.getStock());

        // 2. 适配前端画廊 (前端需要 List<String> images)
        // 因为我们表里只有 cover_image_url，所以把它放进 list
        // 2. 查询商品图集 (从新建的 book_image 表中查询所有图片)
        List<String> images = bookImageMapper.selectObjs(new LambdaQueryWrapper<BookImage>()
                        .select(BookImage::getImageUrl)
                        .eq(BookImage::getBookId, productId)
                        .orderByAsc(BookImage::getSortOrder)) // 按照我们之前存的 sort_order 排序
                .stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        // 容错处理：如果在 book_image 表里没查到图，就退化去查封面图
        if (images.isEmpty()) {
            if (bookInfo.getCoverImageUrl() != null && !bookInfo.getCoverImageUrl().isEmpty()) {
                images.add(bookInfo.getCoverImageUrl());
            } else {
                // 如果连封面都没有，给个默认占位图
                images.add("https://shadow.elemecdn.com/app/element/hamburger.9cf7b091-55e9-11e9-a976-7f4d0b07eef6.png");
            }
        }
        detailVO.setImages(images);

        // 3. 查出这本书绑定的标签 (利用您之前写好的原生 SQL 方法)
        List<String> tags = bookTagMapper.getTagsByBookId(productId);
        detailVO.setTags(tags);

        // 4. 查询前台用户评价 (仅展示审核通过 status=1 的评价，按时间倒序，最多10条)
        List<BookReview> reviewList = bookReviewMapper.selectList(new LambdaQueryWrapper<BookReview>()
                .eq(BookReview::getBookId, productId)
                .eq(BookReview::getStatus, 1)
                .orderByDesc(BookReview::getCreateTime)
                .last("LIMIT 10"));

        List<ReviewVO> reviewVOs = reviewList.stream().map(review -> {
            ReviewVO vo = new ReviewVO();
            vo.setId(review.getId());
            vo.setRating(review.getRating());
            vo.setContent(review.getContent());
            vo.setDate(formatDate(review.getCreateTime()));

            // 查询用户名并脱敏
            SysUser user = sysUserMapper.selectById(review.getUserId());
            if (user != null) {
                vo.setUsername(maskUsername(user.getUsername()));
                // 如果数据库有头像就用，没有给个默认头像
                vo.setAvatar("https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png");
            } else {
                vo.setUsername("匿名用户");
            }

            return vo;
        }).collect(Collectors.toList());

        detailVO.setReviews(reviewVOs);

        return detailVO;
    }

    /**
     * 工具方法：用户名脱敏 (航海时代 -> 航**代)
     */
    private String maskUsername(String username) {
        if (username == null || username.length() <= 1) return username;
        if (username.length() == 2) return username.charAt(0) + "*";
        return username.charAt(0) + "**" + username.charAt(username.length() - 1);
    }

    /**
     * 工具方法：日期格式化 (YYYY-MM-DD)
     */
    private String formatDate(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}