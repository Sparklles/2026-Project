package com.example.productmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.productmanagement.entity.BookInfo;
import com.example.productmanagement.entity.UserFavorite;
import com.example.productmanagement.mapper.BookInfoMapper;
import com.example.productmanagement.mapper.UserFavoriteMapper;
import com.example.productmanagement.service.FrontFavoriteService;
import com.example.productmanagement.vo.FavoriteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FrontFavoriteServiceImpl implements FrontFavoriteService {

    private final UserFavoriteMapper userFavoriteMapper;
    private final BookInfoMapper bookInfoMapper;

    @Override
    public List<FavoriteVO> getUserFavorites(Long userId) {
        LambdaQueryWrapper<UserFavorite> favQuery = new LambdaQueryWrapper<>();
        favQuery.eq(UserFavorite::getUserId, userId).orderByDesc(UserFavorite::getCreateTime);
        List<UserFavorite> favorites = userFavoriteMapper.selectList(favQuery);

        if (favorites.isEmpty()) return new ArrayList<>();

        return favorites.stream().map(fav -> {
            BookInfo book = bookInfoMapper.selectById(fav.getBookId());
            if (book == null) return null;

            FavoriteVO vo = new FavoriteVO();
            vo.setId(book.getId());
            vo.setTitle(book.getTitle());
            vo.setImage(book.getCoverImageUrl() != null ? book.getCoverImageUrl() : "...");
            vo.setPrice(book.getPrice()); // 现在的最新价格

            // 🌟 核心逻辑：动态比对价格
            BigDecimal currentPrice = book.getPrice();
            BigDecimal favPrice = fav.getFavPrice() != null ? fav.getFavPrice() : currentPrice;

            int compareResult = currentPrice.compareTo(favPrice);
            if (compareResult < 0) {
                // 当前价格 < 收藏价格 = 降价了
                vo.setPriceStatus(1);
                vo.setPriceDiff(favPrice.subtract(currentPrice));
            } else if (compareResult > 0) {
                // 当前价格 > 收藏价格 = 涨价了
                vo.setPriceStatus(-1);
                vo.setPriceDiff(currentPrice.subtract(favPrice));
            } else {
                // 价格没变
                vo.setPriceStatus(0);
                vo.setPriceDiff(BigDecimal.ZERO);
            }

            // 其他信息...
            vo.setFavCount((book.getId() % 10 + 1) + "百+");
            vo.setCategory((book.getTitle().contains("仪") || book.getTitle().contains("计")) ? "devices" : "books");
            vo.setIsSelfOperated(book.getId() % 2 == 0);
            vo.setStatus(book.getStatus() == 1 ? 1 : 0);
            vo.setSelected(false);

            return vo;
        }).filter(vo -> vo != null).collect(Collectors.toList());
    }

    @Override
    public void removeFavorites(Long userId, List<Long> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) return;

        LambdaQueryWrapper<UserFavorite> deleteQuery = new LambdaQueryWrapper<>();
        deleteQuery.eq(UserFavorite::getUserId, userId)
                .in(UserFavorite::getBookId, bookIds);
        userFavoriteMapper.delete(deleteQuery);
    }

    @Override
    public void addFavorite(Long userId, Long bookId) {
        LambdaQueryWrapper<UserFavorite> query = new LambdaQueryWrapper<>();
        query.eq(UserFavorite::getUserId, userId).eq(UserFavorite::getBookId, bookId);

        if (userFavoriteMapper.selectCount(query) == 0) {
            BookInfo book = bookInfoMapper.selectById(bookId);
            if (book != null) {
                UserFavorite fav = new UserFavorite();
                fav.setUserId(userId);
                fav.setBookId(bookId);
                // 🌟 核心：插入收藏记录时，顺便把当下的价格存入库中
                fav.setFavPrice(book.getPrice());
                userFavoriteMapper.insert(fav);
            }
        }
    }

    @Override
    public boolean isFavorited(Long userId, Long bookId) {
        LambdaQueryWrapper<UserFavorite> query = new LambdaQueryWrapper<>();
        query.eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getBookId, bookId);
        // 如果查出来的数量大于0，说明已经收藏过了
        return userFavoriteMapper.selectCount(query) > 0;
    }
}
