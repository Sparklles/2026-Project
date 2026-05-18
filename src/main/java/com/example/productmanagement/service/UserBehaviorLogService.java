package com.example.productmanagement.service;

/**
 * 用户行为日志服务。
 *
 * <p>该服务是推荐模块统一的行为埋点入口，负责把前台用户的关键操作写入
 * user_behavior_log 表，后续推荐任务会读取这些行为数据生成个性化推荐结果。</p>
 *
 * <p>业务代码只需要调用语义明确的方法，不直接拼装 UserBehaviorLog 实体，
 * 这样可以避免各业务 service 中行为类型、过期时间等字段写法不一致。</p>
 */
public interface UserBehaviorLogService {

    /**
     * 记录用户浏览书籍详情页行为。
     *
     * @param userId 当前登录用户ID，未登录时传 null 则不写入
     * @param bookId 被浏览的书籍ID
     */
    void recordBrowse(Long userId, Long bookId);

    /**
     * 记录用户搜索行为。
     *
     * @param userId 当前登录用户ID，未登录时传 null 则不写入
     * @param keyword 搜索关键词
     */
    void recordSearch(Long userId, String keyword);

    /**
     * 记录用户加购行为。
     *
     * @param userId 当前登录用户ID
     * @param bookId 加入购物车的书籍ID
     */
    void recordAddCart(Long userId, Long bookId);

    /**
     * 记录用户收藏行为。
     *
     * @param userId 当前登录用户ID
     * @param bookId 被收藏的书籍ID
     */
    void recordFavorite(Long userId, Long bookId);

    /**
     * 记录用户购买行为。
     *
     * @param userId 当前登录用户ID
     * @param bookId 支付成功订单中的书籍ID
     */
    void recordPurchase(Long userId, Long bookId);
}
