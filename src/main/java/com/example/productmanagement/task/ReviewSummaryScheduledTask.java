package com.example.productmanagement.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.productmanagement.entity.BookInfo;
import com.example.productmanagement.mapper.BookInfoMapper;
import com.example.productmanagement.service.impl.AIReviewSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewSummaryScheduledTask {

    private final AIReviewSummaryService aiReviewSummaryService;
    private final BookInfoMapper bookInfoMapper;

    /**
     * 每天凌晨 3:00 自动触发，为近期有新评价的商品生成 AI 总结
     * (这里为了简单，演示为遍历所有上架商品。实际生产中可结合 Redis 记录今日有新评价的商品 ID 集合)
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void executeReviewSummarization() {
        log.info("--- 开始执行每日 AI 评价总结任务 ---");

        // 查询所有在售商品
        QueryWrapper<BookInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1).eq("is_deleted", 0);
        List<BookInfo> activeBooks = bookInfoMapper.selectList(queryWrapper);

        for (BookInfo book : activeBooks) {
            // 逐个处理，Spring AI 在底层会自动管理连接
            aiReviewSummaryService.getOrGenerateReviewSummary(book.getId());

            // 稍作休眠，防止触发大模型 API 的并发限流 (Rate Limit)
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        log.info("--- 每日 AI 评价总结任务执行完毕 ---");
    }
}
