package com.example.productmanagement.service;

import com.example.productmanagement.vo.HomeRecommendVO;
import com.example.productmanagement.vo.RecommendBookVO;

import java.util.List;

public interface RecommendService {
    List<RecommendBookVO> getAlsoBought(Long bookId);
    List<RecommendBookVO> getPersonalized(Long userId);
    HomeRecommendVO getHomeRecommend();
}