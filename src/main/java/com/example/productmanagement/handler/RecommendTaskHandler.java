package com.example.productmanagement.handler;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.productmanagement.entity.BookInfo;
import com.example.productmanagement.entity.BookStats;
import com.example.productmanagement.entity.RecommendConfig;
import com.example.productmanagement.entity.RecommendationItem;
import com.example.productmanagement.entity.User;
import com.example.productmanagement.entity.UserBehaviorLog;
import com.example.productmanagement.mapper.*;
import com.example.productmanagement.strategy.RecommendStrategy;
import com.example.productmanagement.strategy.ScoredBook;
import com.example.productmanagement.strategy.StrategyFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendTaskHandler {

    private final BookStatsMapper bookStatsMapper;
    private final BookInfoMapper bookInfoMapper;
    private final RecommendConfigMapper recommendConfigMapper;
    private final RecommendationItemMapper recommendationItemMapper;
    private final StrategyFactory strategyFactory;
    private final BookScoreCalculator bookScoreCalculator;
    private final UserMapper userMapper;
    private final UserBehaviorLogMapper userBehaviorLogMapper;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Set<String> DAILY_SCENES = Set.of("POPULAR", "NEW", "HOME_TOPIC");
    private static final Set<String> WEEKLY_SCENES = Set.of("ALSO_BOUGHT", "PERSONALIZED");

    @Scheduled(cron = "0 23 3 * * ?")
    @Transactional
    public void executeDailyRecommendTask() {
        log.info("=== 凌晨3点推荐任务开始 ===");
        try {
            refreshRecommendations();
            log.info("=== 凌晨3点推荐任务完成 ===");
        } catch (Exception e) {
            log.error("凌晨3点推荐任务异常", e);
        }
    }

    @Transactional
    public void refreshRecommendations() {
        recalculateCompositeScores();
        generateRecommendations();
    }

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanExpiredRecommendations() {
        log.info("=== 凌晨2点过期推荐清理开始 ===");
        try {
            LambdaQueryWrapper<RecommendationItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.lt(RecommendationItem::getExpireTime, new Date());
            long deleted = recommendationItemMapper.delete(wrapper);
            log.info("清理过期推荐条目: {} 条", deleted);

            LambdaQueryWrapper<UserBehaviorLog> behaviorWrapper = new LambdaQueryWrapper<>();
            behaviorWrapper.lt(UserBehaviorLog::getExpireTime, new Date());
            long behaviorDeleted = userBehaviorLogMapper.delete(behaviorWrapper);
            log.info("清理过期用户行为日志: {} 条", behaviorDeleted);
        } catch (Exception e) {
            log.error("过期推荐清理异常", e);
        }
        log.info("=== 凌晨2点过期推荐清理完成 ===");
    }

    private void recalculateCompositeScores() {
        log.info("开始重新计算综合评分...");
        List<BookStats> allStats = bookStatsMapper.selectList(null);
        if (allStats.isEmpty()) {
            log.info("无书籍统计数据，跳过评分计算");
            return;
        }

        double maxSales = allStats.stream()
                .mapToDouble(s -> s.getSales() != null ? s.getSales().doubleValue() : 0)
                .max()
                .orElse(1.0);

        int updatedCount = 0;
        for (BookStats stats : allStats) {
            if (stats.getAvgRating() == null) {
                stats.setAvgRating(BigDecimal.ZERO);
            }
            double score = bookScoreCalculator.calculate(stats, maxSales);
            stats.setCompositeScore(BigDecimal.valueOf(score).setScale(4, RoundingMode.HALF_UP));
            bookStatsMapper.updateById(stats);
            updatedCount++;
        }
        log.info("综合评分重新计算完成，更新 {} 条记录", updatedCount);
    }

    private void generateRecommendations() {
        List<RecommendConfig> configs = recommendConfigMapper.selectList(
                new LambdaQueryWrapper<RecommendConfig>().eq(RecommendConfig::getStatus, 1));

        for (RecommendConfig config : configs) {
            try {
                processConfig(config);
            } catch (Exception e) {
                log.error("处理推荐配置失败: configKey={}", config.getConfigKey(), e);
            }
        }
    }

    private void processConfig(RecommendConfig config) {
        String sceneCode = config.getSceneCode();
        JsonNode ruleNode;
        try {
            ruleNode = objectMapper.readTree(config.getRuleJson());
        } catch (Exception e) {
            log.error("解析rule_json失败: configKey={}", config.getConfigKey(), e);
            return;
        }

        boolean isDaily = DAILY_SCENES.contains(sceneCode);
        boolean isWeekly = WEEKLY_SCENES.contains(sceneCode);

        if (isDaily) {
            deleteOldAndInsertNew(config, ruleNode, 1);
        } else if (isWeekly) {
            upsertRecommendations(config, ruleNode, 7);
        } else {
            deleteOldAndInsertNew(config, ruleNode, 1);
        }
    }

    private void deleteOldAndInsertNew(RecommendConfig config, JsonNode ruleNode, int expireDays) {
        String sceneCode = config.getSceneCode();

        recommendationItemMapper.delete(
                new LambdaQueryWrapper<RecommendationItem>()
                        .eq(RecommendationItem::getSceneCode, sceneCode));

        List<ScoredBook> scoredBooks = executeStrategies(ruleNode, null, null);
        if (scoredBooks.isEmpty()) {
            log.info("场景[{}]未生成推荐结果", sceneCode);
            return;
        }

        Date expireTime = Date.from(LocalDateTime.now().plusDays(expireDays)
                .atZone(ZoneId.systemDefault()).toInstant());

        int sortOrder = 0;
        for (ScoredBook sb : scoredBooks) {
            RecommendationItem item = new RecommendationItem();
            item.setId(IdUtil.getSnowflake(1, 1).nextId());
            item.setSceneCode(sceneCode);
            item.setTargetBookId(sb.getBookId());
            item.setScore(sb.getScore());
            item.setRecommendReason(sb.getReason());
            item.setSortOrder(sortOrder++);
            item.setExpireTime(expireTime);
            recommendationItemMapper.insert(item);
        }
        log.info("场景[{}]全量刷新完成，插入 {} 条", sceneCode, scoredBooks.size());
    }

    private void upsertRecommendations(RecommendConfig config, JsonNode ruleNode, int expireDays) {
        String sceneCode = config.getSceneCode();
        Date expireTime = Date.from(LocalDateTime.now().plusDays(expireDays)
                .atZone(ZoneId.systemDefault()).toInstant());

        if ("ALSO_BOUGHT".equals(sceneCode)) {
            JsonNode strategiesNode = ruleNode.get("strategies");
            JsonNode firstStrategy = (strategiesNode != null && strategiesNode.isArray() && strategiesNode.size() > 0)
                    ? strategiesNode.get(0) : null;

            int limit = 10;
            Map<String, Double> weights = new LinkedHashMap<>();
            weights.put("coOccurOrder", 0.5);
            weights.put("sameUser", 0.3);
            weights.put("sameCategory", 0.1);
            weights.put("sameTag", 0.1);

            if (firstStrategy != null) {
                JsonNode paramsNode = firstStrategy.get("params");
                if (paramsNode != null) {
                    if (paramsNode.has("limit")) limit = normalizeLimit(paramsNode.get("limit").asInt(), 10);
                    JsonNode wNode = paramsNode.get("weights");
                    if (wNode != null) {
                        weights.clear();
                        wNode.fields().forEachRemaining(e ->
                                weights.put(e.getKey(), e.getValue().asDouble()));
                    }
                }
            }

            List<BookInfo> activeBooks = bookInfoMapper.selectList(
                    new LambdaQueryWrapper<BookInfo>()
                            .eq(BookInfo::getStatus, 1)
                            .eq(BookInfo::getIsDeleted, 0));
            int count = 0;
            for (BookInfo book : activeBooks) {
                Map<String, Object> params = new HashMap<>();
                params.put("limit", limit);
                params.put("sourceBookId", book.getId());
                params.put("weights", weights);

                RecommendStrategy strategy = strategyFactory.getStrategy("ORDER_SIMILARITY");
                if (strategy == null) continue;
                List<ScoredBook> scoredBooks = strategy.execute(params);

                int sortOrder = 0;
                for (ScoredBook sb : scoredBooks) {
                    RecommendationItem existing = recommendationItemMapper.selectOne(
                            new LambdaQueryWrapper<RecommendationItem>()
                                    .eq(RecommendationItem::getSceneCode, sceneCode)
                                    .eq(RecommendationItem::getSourceBookId, book.getId())
                                    .eq(RecommendationItem::getTargetBookId, sb.getBookId()));

                    if (existing != null) {
                        existing.setScore(sb.getScore());
                        existing.setSortOrder(sortOrder);
                        existing.setExpireTime(expireTime);
                        recommendationItemMapper.updateById(existing);
                    } else {
                        RecommendationItem item = new RecommendationItem();
                        item.setId(IdUtil.getSnowflake(1, 1).nextId());
                        item.setSceneCode(sceneCode);
                        item.setSourceBookId(book.getId());
                        item.setTargetBookId(sb.getBookId());
                        item.setScore(sb.getScore());
                        item.setRecommendReason(sb.getReason());
                        item.setSortOrder(sortOrder);
                        item.setExpireTime(expireTime);
                        recommendationItemMapper.insert(item);
                    }
                    sortOrder++;
                }
                count++;
            }
            log.info("场景[ALSO_BOUGHT]更新完成，处理 {} 本书", count);
        } else if ("PERSONALIZED".equals(sceneCode)) {
            List<User> activeUsers = userMapper.selectList(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getStatus, 1)
                            .eq(User::getIsDeleted, 0)
                            .ge(User::getLastLoginTime, LocalDateTime.now().minusDays(7)));

            if (activeUsers.isEmpty()) {
                log.info("场景[PERSONALIZED]无一周内登录的活跃用户，跳过");
                return;
            }

            int totalResults = normalizeLimit(ruleNode.has("totalResults") ? ruleNode.get("totalResults").asInt() : 6, 6);

            int userCount = 0;
            for (User user : activeUsers) {
                List<ScoredBook> scoredBooks = executePersonalizedStrategies(ruleNode, user.getId(), totalResults);
                if (scoredBooks.isEmpty()) {
                    continue;
                }

                int sortOrder = 0;
                for (ScoredBook sb : scoredBooks) {
                    RecommendationItem existing = recommendationItemMapper.selectOne(
                            new LambdaQueryWrapper<RecommendationItem>()
                                    .eq(RecommendationItem::getSceneCode, sceneCode)
                                    .eq(RecommendationItem::getUserId, user.getId())
                                    .eq(RecommendationItem::getTargetBookId, sb.getBookId()));

                    if (existing != null) {
                        existing.setScore(sb.getScore());
                        existing.setSortOrder(sortOrder);
                        existing.setExpireTime(expireTime);
                        recommendationItemMapper.updateById(existing);
                    } else {
                        RecommendationItem item = new RecommendationItem();
                        item.setId(IdUtil.getSnowflake(1, 1).nextId());
                        item.setSceneCode(sceneCode);
                        item.setUserId(user.getId());
                        item.setTargetBookId(sb.getBookId());
                        item.setScore(sb.getScore());
                        item.setRecommendReason(sb.getReason());
                        item.setSortOrder(sortOrder);
                        item.setExpireTime(expireTime);
                        recommendationItemMapper.insert(item);
                    }
                    sortOrder++;
                }
                userCount++;
            }
            log.info("场景[PERSONALIZED]更新完成，处理 {} 个用户", userCount);
        }
    }

    private List<ScoredBook> executePersonalizedStrategies(JsonNode ruleNode, Long userId, int totalResults) {
        List<ScoredBook> result = new ArrayList<>();
        Set<Long> seenBookIds = new HashSet<>();

        JsonNode strategiesNode = ruleNode.get("strategies");
        if (strategiesNode == null || !strategiesNode.isArray()) {
            return result;
        }

        Map<String, JsonNode> strategyMap = new HashMap<>();
        for (JsonNode sn : strategiesNode) {
            strategyMap.put(sn.get("strategyType").asText(), sn);
        }

        int behaviorLimit = 4;
        int categoryLimit = 2;

        JsonNode behaviorNode = strategyMap.get("BEHAVIOR_WEIGHTED");
        if (behaviorNode != null) {
            Map<String, Object> params = buildStrategyParams(behaviorNode, userId, null);
            params.putIfAbsent("limit", behaviorLimit);
            RecommendStrategy strategy = strategyFactory.getStrategy("BEHAVIOR_WEIGHTED");
            if (strategy != null) {
                List<ScoredBook> behaviorResults = strategy.execute(params);
                for (ScoredBook sb : behaviorResults) {
                    if (result.size() >= behaviorLimit) break;
                    if (seenBookIds.add(sb.getBookId())) {
                        result.add(sb);
                    }
                }
            }
        }

        JsonNode categoryNode = strategyMap.get("CATEGORY_PREFERENCE");
        if (categoryNode != null && result.size() < totalResults) {
            Map<String, Object> params = buildStrategyParams(categoryNode, userId, null);
            params.putIfAbsent("limit", categoryLimit);
            RecommendStrategy strategy = strategyFactory.getStrategy("CATEGORY_PREFERENCE");
            if (strategy != null) {
                List<ScoredBook> categoryResults = strategy.execute(params);
                for (ScoredBook sb : categoryResults) {
                    if (result.size() >= behaviorLimit + categoryLimit) break;
                    if (seenBookIds.add(sb.getBookId())) {
                        result.add(sb);
                    }
                }
            }
        }

        if (result.size() < totalResults) {
            JsonNode popularNode = strategyMap.get("POPULAR");
            if (popularNode != null) {
                Map<String, Object> params = buildStrategyParams(popularNode, null, null);
                params.putIfAbsent("limit", totalResults);
                RecommendStrategy strategy = strategyFactory.getStrategy("POPULAR");
                if (strategy != null) {
                    List<ScoredBook> popularResults = strategy.execute(params);
                    for (ScoredBook sb : popularResults) {
                        if (result.size() >= totalResults) break;
                        if (seenBookIds.add(sb.getBookId())) {
                            result.add(sb);
                        }
                    }
                }
            }
        }

        return result;
    }

    private Map<String, Object> buildStrategyParams(JsonNode strategyNode, Long userId, Long sourceBookId) {
        Map<String, Object> params = new HashMap<>();
        JsonNode paramsNode = strategyNode.get("params");
        if (paramsNode != null) {
            paramsNode.fields().forEachRemaining(entry ->
                    params.put(entry.getKey(), parseJsonValue(entry.getValue())));
        }
        if (userId != null) params.put("userId", userId);
        if (sourceBookId != null) params.put("sourceBookId", sourceBookId);
        return params;
    }

    private List<ScoredBook> executeStrategies(JsonNode ruleNode, Long userId, Long sourceBookId) {
        List<ScoredBook> allResults = new ArrayList<>();
        Set<Long> seenBookIds = new HashSet<>();

        JsonNode strategiesNode = ruleNode.get("strategies");
        if (strategiesNode == null || !strategiesNode.isArray()) {
            return allResults;
        }

        int totalResults = normalizeLimit(ruleNode.has("totalResults") ? ruleNode.get("totalResults").asInt() : 6, 6);

        for (JsonNode strategyNode : strategiesNode) {
            String strategyType = strategyNode.get("strategyType").asText();
            double weight = strategyNode.has("weight") ? strategyNode.get("weight").asDouble() : 1.0;

            RecommendStrategy strategy = strategyFactory.getStrategy(strategyType);
            if (strategy == null) {
                log.warn("未找到策略: {}", strategyType);
                continue;
            }

            Map<String, Object> params = new HashMap<>();
            JsonNode paramsNode = strategyNode.get("params");
            if (paramsNode != null) {
                paramsNode.fields().forEachRemaining(entry ->
                        params.put(entry.getKey(), parseJsonValue(entry.getValue())));
            }
            if (userId != null) params.put("userId", userId);
            if (sourceBookId != null) params.put("sourceBookId", sourceBookId);

            List<ScoredBook> results = strategy.execute(params);
            for (ScoredBook sb : results) {
                if (!seenBookIds.contains(sb.getBookId())) {
                    seenBookIds.add(sb.getBookId());
                    BigDecimal weightedScore = sb.getScore().multiply(BigDecimal.valueOf(weight));
                    allResults.add(new ScoredBook(sb.getBookId(), weightedScore, sb.getReason()));
                }
            }
        }

        allResults.sort((a, b) -> b.getScore().compareTo(a.getScore()));
        if (allResults.size() > totalResults) {
            allResults = allResults.subList(0, totalResults);
        }
        return allResults;
    }

    private Object parseJsonValue(JsonNode node) {
        if (node.isInt()) return node.asInt();
        if (node.isLong()) return node.asLong();
        if (node.isFloat() || node.isDouble() || node.isBigDecimal()) return node.asDouble();
        if (node.isTextual()) return node.asText();
        if (node.isBoolean()) return node.asBoolean();
        if (node.isObject()) {
            Map<String, Object> result = new LinkedHashMap<>();
            node.fields().forEachRemaining(entry ->
                    result.put(entry.getKey(), parseJsonValue(entry.getValue())));
            return result;
        }
        if (node.isArray()) {
            List<Object> result = new ArrayList<>();
            node.forEach(item -> result.add(parseJsonValue(item)));
            return result;
        }
        return null;
    }

    private int normalizeLimit(int value, int defaultValue) {
        int limit = value <= 0 ? defaultValue : value;
        return Math.min(limit, 50);
    }
}
