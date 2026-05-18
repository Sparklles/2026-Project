package com.example.productmanagement.strategy;

final class StrategyLimits {

    private StrategyLimits() {
    }

    static int limit(Object value, int defaultValue) {
        return positiveInt(value, defaultValue, 50);
    }

    static int positiveInt(Object value, int defaultValue, int maxValue) {
        int number = defaultValue;
        if (value instanceof Number n) {
            number = n.intValue();
        } else if (value instanceof String text) {
            try {
                number = Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                number = defaultValue;
            }
        }
        if (number <= 0) {
            number = defaultValue;
        }
        return Math.min(number, maxValue);
    }
}
