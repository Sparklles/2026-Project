package com.example.productmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.productmanagement.dto.BookQueryDTO;
import com.example.productmanagement.entity.*;
import com.example.productmanagement.enums.BookSortField;
import com.example.productmanagement.enums.SortOrder;
import com.example.productmanagement.mapper.*;
import com.example.productmanagement.service.FrontBookService;
import com.example.productmanagement.service.UserBehaviorLogService;
import com.example.productmanagement.utils.UserHolder;
import com.example.productmanagement.vo.ProductDetailVO;
import com.example.productmanagement.vo.ReviewVO;
import com.example.productmanagement.vo.SearchBookVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FrontBookServiceImpl extends ServiceImpl<BookInfoMapper, BookInfo> implements FrontBookService {

    @Autowired
    private BookCategoryMapper bookCategoryMapper;
    @Autowired
    private BookTagMapper bookTagMapper;
    @Autowired
    private BookReviewMapper bookReviewMapper;

    // 🌟 1. 核心修复：替换为真正的 UserMapper
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BookImageMapper bookImageMapper;
    @Autowired
    private BookInfoMapper bookInfoMapper;
    @Autowired
    private SearchLogMapper searchLogMapper;
    @Autowired
    private UserBehaviorLogService userBehaviorLogService;

    @Override
    public IPage<SearchBookVO> searchBooks(IPage<SearchBookVO> page, String keyword, String sortField, String sortOrder) {
        BookSortField bookSortField = BookSortField.fromField(sortField);
        SortOrder order = SortOrder.fromValue(sortOrder);
        String safeSortField = bookSortField == null ? null : bookSortField.getColumn();
        String safeSortOrder = order == null ? SortOrder.DESC.getValue() : order.getValue();
        IPage<SearchBookVO> result = baseMapper.searchBooks(page, null, keyword, safeSortField, safeSortOrder);
        recordSearchLog(page, keyword, result.getTotal());
        return result;
    }

    @Override
    public IPage<SearchBookVO> queryBooks(IPage<SearchBookVO> page, BookQueryDTO queryDto) {
        if (queryDto.getSortField() != null) {
            BookSortField sortField = BookSortField.fromField(queryDto.getSortField());
            if (sortField == null) {
                queryDto.setSortField(null);
            } else {
                queryDto.setSortField(sortField.getColumn());
            }
        }
        if (queryDto.getSortOrder() != null) {
            SortOrder sortOrder = SortOrder.fromValue(queryDto.getSortOrder());
            queryDto.setSortOrder(sortOrder != null ? sortOrder.getValue() : SortOrder.DESC.getValue());
        } else {
            queryDto.setSortOrder(SortOrder.DESC.getValue());
        }
        return baseMapper.selectBookPageWithFilters(page, queryDto);
    }

    @Override
    public List<BookCategory> getAllCategories() {
        LambdaQueryWrapper<BookCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(BookCategory::getSortOrder);
        return bookCategoryMapper.selectList(wrapper);
    }

    @Override
    public ProductDetailVO getProductDetail(Long productId) {
        // 1. 查询商品基础信息
        BookInfo bookInfo = bookInfoMapper.selectById(productId);
        if (bookInfo == null || bookInfo.getStatus() == 0) {
            throw new RuntimeException("商品不存在或已下架");
        }
        recordBrowseBehavior(productId);

        ProductDetailVO detailVO = new ProductDetailVO();
        detailVO.setId(bookInfo.getId());
        detailVO.setTitle(bookInfo.getTitle());
        detailVO.setSubtitle(bookInfo.getDescription());
        detailVO.setMinPrice(bookInfo.getPrice());
        detailVO.setStock(bookInfo.getStock());

        // 2. 查询商品图集
        List<String> images = bookImageMapper.selectObjs(new LambdaQueryWrapper<BookImage>()
                        .select(BookImage::getImageUrl)
                        .eq(BookImage::getBookId, productId)
                        .orderByAsc(BookImage::getSortOrder))
                .stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        if (images.isEmpty()) {
            if (bookInfo.getCoverImageUrl() != null && !bookInfo.getCoverImageUrl().isEmpty()) {
                images.add(bookInfo.getCoverImageUrl());
            } else {
                images.add("https://shadow.elemecdn.com/app/element/hamburger.9cf7b091-55e9-11e9-a976-7f4d0b07eef6.png");
            }
        }
        detailVO.setImages(images);

        // 3. 查出这本书绑定的标签
        List<String> tags = bookTagMapper.getTagsByBookId(productId);
        detailVO.setTags(tags);

        // 4. 查询前台用户评价
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

            // 🌟 2. 核心修复：使用真正的 User 实体去查
            User user = userMapper.selectById(review.getUserId());
            if (user != null) {
                // 🌟 3. 核心修复：你的 User 实体里没有 getUsername，使用的是 getLoginAccount()
                vo.setUsername(maskUsername(user.getLoginAccount()));
                vo.setAvatar("https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png");
            } else {
                vo.setUsername("匿名用户");
                vo.setAvatar("https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png");
            }

            return vo;
        }).collect(Collectors.toList());

        detailVO.setReviews(reviewVOs);

        return detailVO;
    }

    private String maskUsername(String username) {
        if (username == null || username.length() <= 1) return username;
        if (username.length() == 2) return username.charAt(0) + "*";
        return username.charAt(0) + "**" + username.charAt(username.length() - 1);
    }

    private String formatDate(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private void recordSearchLog(IPage<SearchBookVO> page, String keyword, long resultCount) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        if (page != null && page.getCurrent() > 1) {
            return;
        }

        try {
            Long userId = UserHolder.getUserId();
            User user = userId == null ? null : userMapper.selectById(userId);

            SearchLog searchLog = new SearchLog();
            searchLog.setUserId(userId);
            searchLog.setUsername(user == null ? null : user.getLoginAccount());
            searchLog.setSearchKeyword(keyword.trim());
            searchLog.setSearchType(1);
            searchLog.setResultCount(resultCount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) resultCount);
            searchLog.setCreateTime(LocalDateTime.now());
            searchLog.setDeleted(0);

            searchLogMapper.insert(searchLog);
            // search_log 用于报表统计，user_behavior_log 用于推荐算法分析搜索偏好。
            userBehaviorLogService.recordSearch(userId, keyword);
        } catch (Exception e) {
            log.warn("Failed to record search log, keyword={}", keyword, e);
        }
    }

    private void recordBrowseBehavior(Long productId) {
        try {
            // 用户打开书籍详情页时记录浏览行为，用于后续个性化推荐。
            userBehaviorLogService.recordBrowse(UserHolder.getUserId(), productId);
        } catch (Exception e) {
            log.warn("Failed to record browse behavior, productId={}", productId, e);
        }
    }
}


