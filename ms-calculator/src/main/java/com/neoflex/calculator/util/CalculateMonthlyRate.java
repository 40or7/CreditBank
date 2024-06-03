package com.neoflex.calculator.util;

import java.math.BigDecimal;
import java.math.MathContext;

import static java.math.MathContext.DECIMAL32;

public class CalculateMonthlyRate {
    public static BigDecimal getMonthlyRate(BigDecimal rate) {
        MathContext mathContext = DECIMAL32;
        return rate.divide(BigDecimal.valueOf(100), mathContext)
                .divide(BigDecimal.valueOf(12), mathContext);
    }
}
