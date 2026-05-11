package com.example.productmanagement.controller.front;

import com.example.productmanagement.entity.BookInfo;
import com.example.productmanagement.result.Result;

import com.example.productmanagement.service.impl.AIRecommendationService;
import com.example.productmanagement.service.impl.ProductVectorSyncService;
import com.example.productmanagement.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai/recommend")
@RequiredArgsConstructor
public class AIRecommendationController {

    private final AIRecommendationService aiRecommendationService;
    private final ProductVectorSyncService productVectorSyncService;

    /**
     * 语义搜索：前端搜索框可以接入这个接口
     * 请求示例: GET /api/ai/recommend/search?query=老人坐船防晕
     */
    @GetMapping("/search")
    public Result<List<BookInfo>> semanticSearch(@RequestParam String query) {
        List<BookInfo> books = aiRecommendationService.semanticSearch(query);
        return Result.ok(books);
    }

    /**
     * 猜你喜欢：前端首页或个人中心底部的推荐区接入这个接口
     */
    @GetMapping("/guess-you-like")
    public Result<List<BookInfo>> guessYouLike() {
        Long userId = UserHolder.getLoginUser().getUserId();
        if (userId == null) {
            // 未登录时的默认推荐
            return Result.ok(aiRecommendationService.semanticSearch("航海商城热销爆款装备"));
        }

        List<BookInfo> books = aiRecommendationService.recommendForUser(userId);
        return Result.ok(books);
    }

    /**
     * 管理员工具：初始化同步全量商品到 Redis 向量库
     * (正常应该放在管理端 Controller，这里为了演示方便写在一起)
     */
    @PostMapping("/admin/sync-vectors")
    public Result<String> syncVectors() {
        // 调用底层大模型计算文本向量并存入 Redis
        productVectorSyncService.syncAllProductsToVectorStore();
        return Result.ok("商品向量化同步成功！");
    }
}
