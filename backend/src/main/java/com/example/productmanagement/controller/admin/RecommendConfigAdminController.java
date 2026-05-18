package com.example.productmanagement.controller.admin;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.productmanagement.controller.Result;
import com.example.productmanagement.entity.RecommendConfig;
import com.example.productmanagement.handler.RecommendTaskHandler;
import com.example.productmanagement.mapper.RecommendConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/recommend-configs")
@RequiredArgsConstructor
public class RecommendConfigAdminController {

    private final RecommendConfigMapper recommendConfigMapper;
    private final RecommendTaskHandler recommendTaskHandler;

    @GetMapping
    public Result<List<RecommendConfig>> listAll() {
        List<RecommendConfig> list = recommendConfigMapper.selectList(
                new LambdaQueryWrapper<RecommendConfig>().orderByAsc(RecommendConfig::getPriority));
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<RecommendConfig> getById(@PathVariable Long id) {
        RecommendConfig config = recommendConfigMapper.selectById(id);
        if (config == null) {
            return Result.error(404, "推荐配置不存在");
        }
        return Result.success(config);
    }

    @PostMapping
    public Result<?> create(@RequestBody RecommendConfig config) {
        Long count = recommendConfigMapper.selectCount(
                new LambdaQueryWrapper<RecommendConfig>().eq(RecommendConfig::getConfigKey, config.getConfigKey()));
        if (count > 0) {
            return Result.error(400, "配置标识 configKey 已存在");
        }
        if (config.getId() == null) {
            config.setId(IdUtil.getSnowflake(1, 1).nextId());
        }
        recommendConfigMapper.insert(config);
        return Result.success("推荐配置创建成功");
    }

    @PostMapping("/refresh")
    public Result<?> refreshRecommendations() {
        recommendTaskHandler.refreshRecommendations();
        return Result.success("推荐刷新任务已执行");
    }

    @PutMapping
    public Result<?> update(@RequestBody RecommendConfig config) {
        if (config.getId() == null) {
            return Result.error(400, "配置ID不能为空");
        }
        RecommendConfig exist = recommendConfigMapper.selectById(config.getId());
        if (exist == null) {
            return Result.error(404, "推荐配置不存在");
        }
        Long count = recommendConfigMapper.selectCount(
                new LambdaQueryWrapper<RecommendConfig>()
                        .eq(RecommendConfig::getConfigKey, config.getConfigKey())
                        .ne(RecommendConfig::getId, config.getId()));
        if (count > 0) {
            return Result.error(400, "配置标识 configKey 已存在");
        }
        recommendConfigMapper.updateById(config);
        return Result.success("推荐配置更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        RecommendConfig exist = recommendConfigMapper.selectById(id);
        if (exist == null) {
            return Result.error(404, "推荐配置不存在");
        }
        recommendConfigMapper.deleteById(id);
        return Result.success("推荐配置删除成功");
    }

    @PutMapping("/{id}/status")
    public Result<?> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        RecommendConfig exist = recommendConfigMapper.selectById(id);
        if (exist == null) {
            return Result.error(404, "推荐配置不存在");
        }
        exist.setStatus(status);
        recommendConfigMapper.updateById(exist);
        return Result.success(status == 1 ? "已启用" : "已禁用");
    }
}
