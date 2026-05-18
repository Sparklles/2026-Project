package com.example.productmanagement.strategy;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class StrategyFactory {

    private final Map<String, RecommendStrategy> strategyMap;

    public StrategyFactory(List<RecommendStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(RecommendStrategy::getType, Function.identity()));
    }

    public RecommendStrategy getStrategy(String type) {
        return strategyMap.get(type);
    }
}