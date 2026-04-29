package com.example.productmanagement.service;

import com.example.productmanagement.vo.FavoriteVO;
import java.util.List;

public interface FrontFavoriteService {
    /** 获取用户的收藏列表 */
    List<FavoriteVO> getUserFavorites(Long userId);

    /** 取消收藏 (支持传入一个或多个商品ID) */
    void removeFavorites(Long userId, List<Long> bookIds);

    /** 添加收藏 */
    void addFavorite(Long userId, Long bookId);

    boolean isFavorited(Long userId, Long bookId);
}
