package com.example.productmanagement.controller.front;

import com.example.productmanagement.entity.BookStats;
import com.example.productmanagement.mapper.BookStatsMapper;
import com.example.productmanagement.result.Result;
import com.example.productmanagement.service.impl.AIReviewSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class BookReviewController {

    private final BookStatsMapper bookStatsMapper;
    private final AIReviewSummaryService aiReviewSummaryService;

    /**
     * 前端详情页获取商品的 AI 评价总结
     */
    @GetMapping("/summary/{bookId}")
    public Result<String> getAiSummary(@PathVariable Long bookId) {
        // 抛弃以前只查数据库的被动逻辑！
        // 直接调用 Service，它内部已经实现了：“有缓存拿缓存，没缓存呼叫大模型现场生成并返回” 的完美闭环。
        String summaryJson = aiReviewSummaryService.getOrGenerateReviewSummary(bookId);

        return Result.ok(summaryJson);
    }

    /**
     * 后台管理员接口：手动触发重新生成某个商品的 AI 总结
     */
    @PostMapping("/admin/force-summarize/{bookId}")
    public Result<String> forceSummarize(@PathVariable Long bookId) {
        // 通常这类耗时操作可以丢入线程池异步执行
        new Thread(() -> aiReviewSummaryService.getOrGenerateReviewSummary(bookId)).start();
        return Result.ok("已触发后台异步生成，请稍后刷新查看");
    }
}
