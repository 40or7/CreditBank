package com.neoflex.calculator.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class CalculateAge {

    public static long getAge(LocalDate birthDate) {
        return ChronoUnit.YEARS.between(birthDate, LocalDate.now());
    }
}