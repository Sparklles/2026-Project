package com.example.productmanagement.strategy;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ScoredBook {
    private Long bookId;
    private BigDecimal score;
    private String reason;
}