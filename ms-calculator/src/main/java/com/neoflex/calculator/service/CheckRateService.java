package com.neoflex.calculator.service;

import com.neoflex.calculator.dto.ScoringDataDto;
import com.neoflex.calculator.enums.FamilyStatus;
import com.neoflex.calculator.enums.Gender;
import com.neoflex.calculator.enums.WorkingPosition;
import com.neoflex.calculator.enums.WorkingStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;


import java.math.BigDecimal;

import static com.neoflex.calculator.enums.FamilyStatus.DIVORCED;
import static com.neoflex.calculator.enums.FamilyStatus.MARRIED;
import static com.neoflex.calculator.enums.WorkingPosition.MANAGER;
import static com.neoflex.calculator.enums.WorkingPosition.TOP_MANAGER;
import static com.neoflex.calculator.enums.WorkingStatus.BUSINESS_OWNER;
import static com.neoflex.calculator.enums.WorkingStatus.SELF_EMPLOYED;
import static com.neoflex.calculator.util.CalculateAge.getAge;
import static java.math.BigDecimal.ZERO;


@AllArgsConstructor
@Component
@Slf4j
@PropertySource("classpath:application.properties")
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "calculator")
public class CheckRateService {

    @Value("${calculator.base-rate:15}")
    private BigDecimal BASE_RATE;

    @Value("${calculator.insurance.rate-reduction:-3}")
    private BigDecimal INSURANCE_RATE_REDUCTION;

    @Value("${calculator.client.rate-reduction:1.1}")
    private BigDecimal CLIENT_RATE_REDUCTION;

    @Value("${calculator.working-status.self-employed-rate-addition:1}")
    private BigDecimal SELF_EMPLOYED_RATE_ADDITION;

    @Value("${calculator.working-status.business-owner-rate-addition:3}")
    private BigDecimal BUSINESS_OWNER_RATE_ADDITION;

    @Value("${calculator.working-position.manager-rate-reduction:-2}")
    private BigDecimal MANAGER_RATE_REDUCTION;

    @Value("${calculator.working-position.top-manager-rate-reduction:-3}")
    private BigDecimal TOP_MANAGER_RATE_REDUCTION;

    @Value("${calculator.family-status.married-rate-reduction:-3}")
    private BigDecimal MARRIED_RATE_REDUCTION;

    @Value("${calculator.family-status.divorced-rate-addition:1}")
    private BigDecimal DIVORCED_RATE_ADDITION;

    @Value("${calculator.gender.male-rate-reduction:-3}")
    private BigDecimal MALE_RATE_REDUCTION;

    @Value("${calculator.gender.female-rate-reduction:-3}")
    private BigDecimal FEMALE_RATE_REDUCTION;

    @Value("${calculator.gender.non-binary-rate-addition:7}")
    private BigDecimal NON_BINARY_RATE_ADDITION;

    public BigDecimal calculatePrescoringRate(boolean isInsuranceEnabled, boolean isSalaryClient) {
        log.info("Calculating prescoring rate");
        return BASE_RATE.add(isInsuranceEnabled ? INSURANCE_RATE_REDUCTION : ZERO)
                .add(isSalaryClient ? CLIENT_RATE_REDUCTION : ZERO);
    }

    public BigDecimal calculateScoringRate(ScoringDataDto scoringDataDto) {
        log.info("Calculating scoring rate");
        BigDecimal rate = calculatePrescoringRate(scoringDataDto.getIsInsuranceEnabled(), scoringDataDto.getIsSalaryClient());
        return rate.add(checkRateByEmployment(scoringDataDto.getEmployment().getWorkingStatus()))
                .add(checkRateByWorkingPosition(scoringDataDto.getEmployment().getWorkingPosition()))
                .add(checkRateByFamilyStatus(scoringDataDto.getFamilyStatus()))
                .add(tariffPremiumBasedOnGender(scoringDataDto.getGender(), getAge(scoringDataDto.getBirthdate())));
    }

    private BigDecimal checkRateByEmployment(WorkingStatus workingStatus) {
        if (SELF_EMPLOYED.equals(workingStatus)) {
            return SELF_EMPLOYED_RATE_ADDITION;
        }
        if (BUSINESS_OWNER.equals(workingStatus)) {
            return BUSINESS_OWNER_RATE_ADDITION;
        }
        return ZERO;
    }

    private BigDecimal checkRateByWorkingPosition(WorkingPosition workingPosition) {
        if (MANAGER.equals(workingPosition)) {
            return MANAGER_RATE_REDUCTION;
        }
        if (TOP_MANAGER.equals(workingPosition)) {
            return TOP_MANAGER_RATE_REDUCTION;
        }
        return ZERO;
    }

    private BigDecimal checkRateByFamilyStatus(FamilyStatus familyStatus) {
        if (MARRIED.equals(familyStatus)) {
            return MARRIED_RATE_REDUCTION;
        }
        if (DIVORCED.equals(familyStatus)) {
            return DIVORCED_RATE_ADDITION;
        }
        return ZERO;
    }

    private BigDecimal tariffPremiumBasedOnGender(Gender gender, long age) {
        if (gender.equals(Gender.FEMALE) && (age >= 32 && age <= 60)) {
            return FEMALE_RATE_REDUCTION;
        } else if (gender.equals(Gender.MALE) && (age >= 30 && age <= 50)) {
            return MALE_RATE_REDUCTION;
        } else if (age >= 18){
            return NON_BINARY_RATE_ADDITION;
        }else {
            return ZERO;
        }
    }
}