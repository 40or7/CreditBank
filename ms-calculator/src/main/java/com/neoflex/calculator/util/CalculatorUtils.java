package com.neoflex.calculator.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static java.math.MathContext.DECIMAL32;

/*
 *CalculatorUtils
 *
 * @author Shilin Vyacheslav
 */
public class CalculatorUtils {

    public static long getAge(LocalDate birthDate) {
        return ChronoUnit.YEARS.between(birthDate, LocalDate.now());
    }


    public static BigDecimal getMonthlyRate(BigDecimal rate) {
        MathContext mathContext = DECIMAL32;
        return rate.divide(BigDecimal.valueOf(100), mathContext)
                .divide(BigDecimal.valueOf(12), mathContext);
    }
}