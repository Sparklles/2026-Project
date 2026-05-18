package com.example.productmanagement.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.productmanagement.entity.UserBehaviorLog;
import com.example.productmanagement.mapper.UserBehaviorLogMapper;
import com.example.productmanagement.service.UserBehaviorLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 用户行为日志服务实现。
 *
 * <p>这里统一封装 user_behavior_log 的写入逻辑：生成主键、设置行为类型、
 * 设置创建时间和过期时间。推荐算法只关心行为数据本身，因此业务 service
 * 不需要感知表字段细节。</p>
 *
 * <p>行为日志属于推荐模块的辅助数据，写入失败时只记录 warn 日志，
 * 不向外抛异常，避免埋点问题影响搜索、加购、收藏、支付等主业务。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserBehaviorLogServiceImpl implements UserBehaviorLogService {

    /** 行为类型：浏览书籍详情。 */
    private static final int BROWSE = 1;
    /** 行为类型：搜索关键词。 */
    private static final int SEARCH = 2;
    /** 行为类型：加入购物车。 */
    private static final int ADD_CART = 4;
    /** 行为类型：收藏书籍。 */
    private static final int FAVORITE = 5;
    /** 行为类型：支付成功购买。 */
    private static final int PURCHASE = 6;
    /** 行为日志默认保留 180 天，过期后由推荐定时任务清理。 */
    private static final int RETENTION_DAYS = 180;

    private final UserBehaviorLogMapper userBehaviorLogMapper;

    @Override
    public void recordBrowse(Long userId, Long bookId) {
        record(userId, bookId, BROWSE, null);
    }

    @Override
    public void recordSearch(Long userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        record(userId, null, SEARCH, keyword.trim());
    }

    @Override
    public void recordAddCart(Long userId, Long bookId) {
        record(userId, bookId, ADD_CART, null);
    }

    @Override
    public void recordFavorite(Long userId, Long bookId) {
        record(userId, bookId, FAVORITE, null);
    }

    @Override
    public void recordPurchase(Long userId, Long bookId) {
        record(userId, bookId, PURCHASE, null);
    }

    /**
     * 通用写入方法。
     *
     * <p>当前推荐算法按登录用户分析偏好，因此 userId 为空时直接跳过。
     * 对于搜索行为只保存 keyword；对于书籍相关行为保存 bookId。</p>
     */
    private void record(Long userId, Long bookId, Integer behaviorType, String keyword) {
        if (userId == null || behaviorType == null) {
            return;
        }
        if (bookId == null && (keyword == null || keyword.isBlank())) {
            return;
        }

        try {
            Date now = new Date();
            UserBehaviorLog logRecord = new UserBehaviorLog();
            logRecord.setId(IdWorker.getId());
            logRecord.setUserId(userId);
            logRecord.setBookId(bookId);
            logRecord.setBehaviorType(behaviorType);
            logRecord.setSearchKeyword(keyword);
            logRecord.setCreateTime(now);
            logRecord.setUpdateTime(now);
            logRecord.setExpireTime(toDate(LocalDateTime.now().plusDays(RETENTION_DAYS)));

            userBehaviorLogMapper.insert(logRecord);
        } catch (Exception e) {
            log.warn("Failed to record user behavior, userId={}, bookId={}, behaviorType={}",
                    userId, bookId, behaviorType, e);
        }
    }

    private Date toDate(LocalDateTime time) {
        return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }
}
