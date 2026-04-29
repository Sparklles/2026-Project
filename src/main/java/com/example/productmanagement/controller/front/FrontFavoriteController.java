package com.example.productmanagement.controller.front;

import com.example.productmanagement.controller.Result; // 替换为你自己的Result路径
import com.example.productmanagement.service.FrontFavoriteService;
import com.example.productmanagement.vo.FavoriteVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/front/favorites")
public class FrontFavoriteController {

    @Autowired
    private FrontFavoriteService favoriteService;

    // 假设当前登录用户的ID为2 (后续请替换为通过 Token 获取)
    private final Long MOCK_USER_ID = 2L;

    /**
     * 1. 获取我的收藏列表
     */
    @GetMapping("/list")
    public Result<List<FavoriteVO>> getList() {
        return Result.success(favoriteService.getUserFavorites(MOCK_USER_ID));
    }

    /**
     * 2. 取消收藏 (支持批量，前端传 [id1, id2...])
     */
    @DeleteMapping("/remove")
    public Result<?> removeFavorites(@RequestBody List<Long> bookIds) {
        favoriteService.removeFavorites(MOCK_USER_ID, bookIds);
        return Result.success("取消收藏成功");
    }

    /**
     * 3. 添加收藏 (给商品详情页用的接口)
     */
    @PostMapping("/add/{bookId}")
    public Result<?> addFavorite(@PathVariable Long bookId) {
        favoriteService.addFavorite(MOCK_USER_ID, bookId);
        return Result.success("收藏成功");
    }

    @GetMapping("/check/{bookId}")
    public Result<Boolean> checkFavorite(@PathVariable Long bookId) {
        return Result.success(favoriteService.isFavorited(MOCK_USER_ID, bookId));
    }
}
