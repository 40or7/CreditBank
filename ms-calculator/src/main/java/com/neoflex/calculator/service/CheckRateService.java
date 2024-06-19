package com.neoflex.calculator.service;

import com.neoflex.calculator.dto.ScoringDataDto;
import com.neoflex.calculator.enums.FamilyStatus;
import com.neoflex.calculator.enums.Gender;
import com.neoflex.calculator.enums.WorkingPosition;
import com.neoflex.calculator.enums.WorkingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.math.BigDecimal;

import static com.neoflex.calculator.enums.FamilyStatus.DIVORCED;
import static com.neoflex.calculator.enums.FamilyStatus.MARRIED;
import static com.neoflex.calculator.enums.WorkingPosition.MANAGER;
import static com.neoflex.calculator.enums.WorkingPosition.TOP_MANAGER;
import static com.neoflex.calculator.enums.WorkingStatus.BUSINESS_OWNER;
import static com.neoflex.calculator.enums.WorkingStatus.SELF_EMPLOYED;
import static com.neoflex.calculator.util.CalculatorUtils.getAge;
import static java.math.BigDecimal.ZERO;

/*
 *CheckRateService
 *
 * @author Shilin Vyacheslav
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class CheckRateService {

    @Value("${calculator.base-rate:15}")
    private BigDecimal baseRate;

    @Value("${calculator.insurance.rate-reduction:-3}")
    private BigDecimal insuranceRateReduction;

    @Value("${calculator.client.rate-reduction:1.1}")
    private BigDecimal clientRateReduction;

    @Value("${calculator.working-status.self-employed-rate-addition:1}")
    private BigDecimal selfEmployedRateAddition;

    @Value("${calculator.working-status.business-owner-rate-addition:3}")
    private BigDecimal businessOwnerRateAddition;

    @Value("${calculator.working-position.manager-rate-reduction:-2}")
    private BigDecimal managerRateReduction;

    @Value("${calculator.working-position.top-manager-rate-reduction:-3}")
    private BigDecimal topManagerRateReduction;

    @Value("${calculator.family-status.married-rate-reduction:-3}")
    private BigDecimal marriedRateReduction;

    @Value("${calculator.family-status.divorced-rate-addition:1}")
    private BigDecimal divorcedRateAddition;

    @Value("${calculator.gender.male-rate-reduction:-3}")
    private BigDecimal maleRateReduction;

    @Value("${calculator.gender.female-rate-reduction:-3}")
    private BigDecimal femaleRateReduction;

    @Value("${calculator.gender.non-binary-rate-addition:7}")
    private BigDecimal nonBinaryRateAddition;

    public BigDecimal calculatePrescoringRate(boolean isInsuranceEnabled, boolean isSalaryClient) {
        log.info("Calculating prescoring rate");
        return baseRate.add(isInsuranceEnabled ? insuranceRateReduction : ZERO)
                .add(isSalaryClient ? clientRateReduction : ZERO);
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
            return selfEmployedRateAddition;
        }
        if (BUSINESS_OWNER.equals(workingStatus)) {
            return businessOwnerRateAddition;
        }
        return ZERO;
    }

    private BigDecimal checkRateByWorkingPosition(WorkingPosition workingPosition) {
        if (MANAGER.equals(workingPosition)) {
            return managerRateReduction;
        }
        if (TOP_MANAGER.equals(workingPosition)) {
            return topManagerRateReduction;
        }
        return ZERO;
    }

    private BigDecimal checkRateByFamilyStatus(FamilyStatus familyStatus) {
        if (MARRIED.equals(familyStatus)) {
            return marriedRateReduction;
        }
        if (DIVORCED.equals(familyStatus)) {
            return divorcedRateAddition;
        }
        return ZERO;
    }

    private BigDecimal tariffPremiumBasedOnGender(Gender gender, long age) {
        if (gender.equals(Gender.FEMALE) && (age >= 32 && age <= 60)) {
            return femaleRateReduction;
        } else if (gender.equals(Gender.MALE) && (age >= 30 && age <= 50)) {
            return maleRateReduction;
        } else if (age >= 18){
            return nonBinaryRateAddition;
        }else {
            return ZERO;
        }
    }
}