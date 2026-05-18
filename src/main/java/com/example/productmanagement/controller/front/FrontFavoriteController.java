package com.example.productmanagement.controller.front;

import com.example.productmanagement.controller.Result; // 替换为你自己的Result路径
import com.example.productmanagement.service.FrontFavoriteService;
import com.example.productmanagement.utils.UserHolder;
import com.example.productmanagement.vo.FavoriteVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/front/favorites")
public class FrontFavoriteController {

    @Autowired
    private FrontFavoriteService favoriteService;

    /**
     * 1. 获取我的收藏列表
     */
    @GetMapping("/list")
    public Result<List<FavoriteVO>> getList() {
        return Result.success(favoriteService.getUserFavorites(currentUserId()));
    }

    /**
     * 2. 取消收藏 (支持批量，前端传 [id1, id2...])
     */
    @DeleteMapping("/remove")
    public Result<?> removeFavorites(@RequestBody List<Long> bookIds) {
        favoriteService.removeFavorites(currentUserId(), bookIds);
        return Result.success("取消收藏成功");
    }

    /**
     * 3. 添加收藏 (给商品详情页用的接口)
     */
    @PostMapping("/add/{bookId}")
    public Result<?> addFavorite(@PathVariable Long bookId) {
        favoriteService.addFavorite(currentUserId(), bookId);
        return Result.success("收藏成功");
    }

    @GetMapping("/check/{bookId}")
    public Result<Boolean> checkFavorite(@PathVariable Long bookId) {
        return Result.success(favoriteService.isFavorited(currentUserId(), bookId));
    }
    private Long currentUserId() {
        return UserHolder.getUserId();
    }
}

