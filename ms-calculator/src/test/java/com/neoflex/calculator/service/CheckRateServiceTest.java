package com.neoflex.calculator.service;

import com.neoflex.calculator.dto.EmploymentDto;
import com.neoflex.calculator.dto.ScoringDataDto;
import com.neoflex.calculator.enums.FamilyStatus;
import com.neoflex.calculator.enums.Gender;
import com.neoflex.calculator.enums.WorkingPosition;
import com.neoflex.calculator.enums.WorkingStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Profile("test")
public class CheckRateServiceTest {

        private final BigDecimal BASE_RATE = BigDecimal.valueOf(15);

        private final BigDecimal INSURANCE_RATE_REDUCTION = BigDecimal.valueOf(-3);

        private final BigDecimal CLIENT_RATE_REDUCTION = BigDecimal.valueOf(-1);

        private final BigDecimal SELF_EMPLOYED_RATE_ADDITION = BigDecimal.valueOf(1);

        private final BigDecimal MANAGER_RATE_REDUCTION = BigDecimal.valueOf(-2);

        private final BigDecimal TOP_MANAGER_RATE_REDUCTION = BigDecimal.valueOf(-3);

        private final BigDecimal MARRIED_RATE_REDUCTION = BigDecimal.valueOf(-3);

        private final BigDecimal DIVORCED_RATE_ADDITION = BigDecimal.valueOf(1);

        private final BigDecimal MALE_RATE_REDUCTION = BigDecimal.valueOf(-3);

        private final BigDecimal NON_BINARY_RATE_ADDITION = BigDecimal.valueOf(7);

        @Autowired
        CheckRateService checkRateService;

        @Test
        void calculatePrescoringRate() {
            assertEquals(BASE_RATE, checkRateService.calculatePrescoringRate(false, false));
            assertEquals(BASE_RATE.add(CLIENT_RATE_REDUCTION), checkRateService.calculatePrescoringRate(false, true));
            assertEquals(BASE_RATE.add(INSURANCE_RATE_REDUCTION), checkRateService.calculatePrescoringRate(true, false));
            assertEquals(BASE_RATE.add(CLIENT_RATE_REDUCTION)
                    .add(INSURANCE_RATE_REDUCTION), checkRateService.calculatePrescoringRate(true, true));
        }

        @Test
        void calculateScoringRate() {
            ScoringDataDto scoringDataDtoFirst = ScoringDataDto.builder()
                    .employment(EmploymentDto.builder()
                            .workingPosition(WorkingPosition.TOP_MANAGER)
                            .workingStatus(WorkingStatus.EMPLOYED)
                            .build())
                    .familyStatus(FamilyStatus.MARRIED)
                    .gender(Gender.MALE)
                    .isInsuranceEnabled(true)
                    .isSalaryClient(true)
                    .birthdate(LocalDate.now().minusYears(40))
                    .build();
            BigDecimal rateFirst = checkRateService.calculateScoringRate(scoringDataDtoFirst);
            assertNotNull(rateFirst);
            assertEquals(BASE_RATE.add(CLIENT_RATE_REDUCTION)
                            .add(INSURANCE_RATE_REDUCTION)
                            .add(TOP_MANAGER_RATE_REDUCTION)
                            .add(MARRIED_RATE_REDUCTION)
                            .add(MALE_RATE_REDUCTION),
                    rateFirst);

            ScoringDataDto scoringDataDtoSecond = ScoringDataDto.builder()
                    .employment(EmploymentDto.builder()
                            .workingPosition(WorkingPosition.MANAGER)
                            .workingStatus(WorkingStatus.SELF_EMPLOYED)
                            .build())
                    .familyStatus(FamilyStatus.DIVORCED)
                    .gender(Gender.NON_BINARY)
                    .isInsuranceEnabled(false)
                    .isSalaryClient(false)
                    .birthdate(LocalDate.now().minusYears(28))
                    .build();
            BigDecimal rateSecond = checkRateService.calculateScoringRate(scoringDataDtoSecond);
            assertNotNull(rateSecond);
            assertEquals(BASE_RATE.add(MANAGER_RATE_REDUCTION)
                            .add(SELF_EMPLOYED_RATE_ADDITION)
                            .add(DIVORCED_RATE_ADDITION)
                            .add(NON_BINARY_RATE_ADDITION),
                    rateSecond);
        }
}
