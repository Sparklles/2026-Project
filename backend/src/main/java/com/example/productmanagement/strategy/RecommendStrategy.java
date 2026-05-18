package com.example.productmanagement.strategy;

import java.util.List;
import java.util.Map;

public interface RecommendStrategy {
    String getType();
    List<ScoredBook> execute(Map<String, Object> params);
}