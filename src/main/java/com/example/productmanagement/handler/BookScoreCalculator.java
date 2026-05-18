package com.example.productmanagement.handler;

import com.example.productmanagement.entity.BookStats;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BookScoreCalculator {
    // 权重可定义为静态常量，未来如需动态可从配置中心获取
    private static final double W_SALES = 0.5;
    private static final double W_RATING = 0.3;
    private static final double W_REVIEW = 0.1;
    private static final double W_FAVORITE = 0.1;

    public double calculate(BookStats stats, double maxSales) {
        if (stats == null) {
            return 0;
        }
        double safeMaxSales = maxSales <= 0 ? 1 : maxSales;
        int sales = stats.getSales() == null ? 0 : stats.getSales();
        int reviewCount = stats.getReviewCount() == null ? 0 : stats.getReviewCount();
        int favoriteCount = stats.getFavoriteCount() == null ? 0 : stats.getFavoriteCount();
        BigDecimal avgRating = stats.getAvgRating() == null ? BigDecimal.ZERO : stats.getAvgRating();

        double salesScore = sales / safeMaxSales * 100;
        double ratingScore = avgRating.doubleValue() * 20;
        double reviewScore = Math.min(reviewCount, 100) * 1.0;
        double favScore = Math.min(favoriteCount, 100) * 1.0;
        return salesScore * W_SALES + ratingScore * W_RATING + reviewScore * W_REVIEW + favScore * W_FAVORITE;
    }
}
