package com.example.productmanagement.vo;

import lombok.Data;
import java.util.List;

@Data
public class HomeRecommendVO {
    private List<ThemeRecommendVO> homeTopic;
    private List<RecommendBookVO> popular;
    private List<RecommendBookVO> newBooks;
}