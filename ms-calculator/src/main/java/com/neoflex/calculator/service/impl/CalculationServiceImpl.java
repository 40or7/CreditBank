package com.neoflex.calculator.service.impl;

import com.neoflex.calculator.dto.*;
import com.neoflex.calculator.exception.ClientRejected;
import com.neoflex.calculator.service.CalculationService;
import com.neoflex.calculator.service.CheckRateService;
import com.neoflex.calculator.service.LoanCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.neoflex.calculator.enums.WorkingStatus.UNEMPLOYED;
import static com.neoflex.calculator.util.CalculatorUtils.getAge;

/*
 *CalculationServiceImpl
 *
 * @author Shilin Vyacheslav
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CalculationServiceImpl implements CalculationService {

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    private final CheckRateService checkRateService;
    private final LoanCalculator loanCalculator;

    @Override
    public List<LoanOfferDto> generateLoanOffers(LoanStatementRequestDto loanStatementRequestDto) {
        log.info("Generating loan offers");
        return List.of(
                generateLoanOffer(loanStatementRequestDto, false, false),
                generateLoanOffer(loanStatementRequestDto, false, true),
                generateLoanOffer(loanStatementRequestDto, true, false),
                generateLoanOffer(loanStatementRequestDto, true, true)
        );
    }

    @Override
    public CreditDto calculateCredit(ScoringDataDto scoringDataDto) {
        log.info("Calculating credit");
        if (isDenied(scoringDataDto)) {
            throw new ClientRejected(String.format("Denied loan for account: %s", scoringDataDto.getAccountNumber()));
        }
        BigDecimal amount = loanCalculator.calculateAmount(scoringDataDto.getAmount(), scoringDataDto.getIsInsuranceEnabled());
        int term = scoringDataDto.getTerm();
        BigDecimal rate = checkRateService.calculateScoringRate(scoringDataDto);
        BigDecimal monthlyPayment = loanCalculator.monthlyInstallments(amount, term, rate);
        BigDecimal psk = loanCalculator.calculatePSK(monthlyPayment, term);
        List<PaymentScheduleElementDto> paymentSchedule = loanCalculator.calculatePaymentSchedule(amount, term, monthlyPayment, rate);
        return CreditDto.builder()
                .amount(amount)
                .term(term)
                .monthlyPayment(monthlyPayment.setScale(2, ROUNDING_MODE))
                .rate(rate)
                .psk(psk)
                .isInsuranceEnabled(scoringDataDto.getIsInsuranceEnabled())
                .isSalaryClient(scoringDataDto.getIsSalaryClient())
                .paymentSchedule(paymentSchedule)
                .build();
    }

    private LoanOfferDto generateLoanOffer(LoanStatementRequestDto loanStatementRequest, boolean isInsuranceEnabled, boolean isSalaryClient) {
        log.info("Generating loan offer");
        BigDecimal amount = loanStatementRequest.getAmount();
        BigDecimal rate = checkRateService.calculatePrescoringRate(isInsuranceEnabled, isSalaryClient);
        BigDecimal totalAmount = loanCalculator.calculateAmount(amount, isInsuranceEnabled);
        int term = loanStatementRequest.getTerm();
        BigDecimal monthlyPayment = loanCalculator.monthlyInstallments(amount, term, rate, ROUNDING_MODE);
        return LoanOfferDto.builder()
                .requestedAmount(amount)
                .totalAmount(totalAmount)
                .term(term)
                .monthlyPayment(monthlyPayment)
                .rate(rate)
                .isInsuranceEnabled(isInsuranceEnabled)
                .isSalaryClient(isSalaryClient)
                .build();
    }

    private boolean isDenied(ScoringDataDto scoringDataDto) {
        if (UNEMPLOYED.equals(scoringDataDto.getEmployment().getWorkingStatus())) {
            return true;
        }
        BigDecimal salary = scoringDataDto.getEmployment().getSalary();
        if (scoringDataDto.getAmount().compareTo(salary.multiply(BigDecimal.valueOf(25))) > 0) {
            return true;
        }
        long age = getAge(scoringDataDto.getBirthdate());
        if (age > 60 || age < 20) {
            return true;
        }
        if (scoringDataDto.getEmployment().getWorkExperienceTotal() < 18) {
            return true;
        }
        return scoringDataDto.getEmployment().getWorkExperienceCurrent() < 3;
    }
}