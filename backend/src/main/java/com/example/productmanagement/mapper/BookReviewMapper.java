package com.example.productmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.example.productmanagement.entity.BookReview;
import com.example.productmanagement.vo.ReviewDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface BookReviewMapper extends BaseMapper<BookReview> {

    /**
     * 后台管控：动态查询评价列表（包含评价人用户名和被评价的书名）
     */
    List<ReviewDetailVO> selectReviewListForAdmin(@Param("rating") Integer rating,
                                                  @Param("status") Integer status);
}