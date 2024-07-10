package com.neoflex.calculator.service.impl;

import com.neoflex.calculator.dto.*;
import com.neoflex.calculator.enums.WorkingStatus;
import com.neoflex.calculator.exception.ClientRejected;
import com.neoflex.calculator.service.CheckRateService;
import com.neoflex.calculator.service.LoanCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

@ExtendWith(MockitoExtension.class)
public class CalculationServiceImplTest {


        private final BigDecimal AMOUNT = BigDecimal.valueOf(20_000);

        private final int TERM = 36;

        private final BigDecimal RATE = BigDecimal.valueOf(12);

        private final BigDecimal MONTHLY_PAYMENT = BigDecimal.valueOf(664.29);

        @InjectMocks
        private CalculationServiceImpl calculationService;

        @Mock
        private CheckRateService mockCheckRateService;

        @Mock
        private LoanCalculator mockLoanCalculator;

        @Test
        void generateLoanOffers() {
            LoanStatementRequestDto loanStatementRequestDto = LoanStatementRequestDto.builder()
                    .amount(AMOUNT)
                    .term(TERM)
                    .build();

            Mockito.when(mockCheckRateService.calculatePrescoringRate(Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(RATE);
            Mockito.when(mockLoanCalculator.calculateAmount(Mockito.any(), Mockito.anyBoolean())).thenReturn(AMOUNT);
            Mockito.when(mockLoanCalculator.monthlyInstallments(Mockito.any(), Mockito.anyInt(), Mockito.any(), Mockito.any())).thenReturn(MONTHLY_PAYMENT);

            List<LoanOfferDto> loanOffers = calculationService.generateLoanOffers(loanStatementRequestDto);

            Mockito.verify(mockCheckRateService, Mockito.times(4)).calculatePrescoringRate(Mockito.anyBoolean(), Mockito.anyBoolean());
            Mockito.verify(mockLoanCalculator, Mockito.times(4)).calculateAmount(Mockito.eq(AMOUNT), Mockito.anyBoolean());
            Mockito.verify(mockLoanCalculator, Mockito.times(4)).monthlyInstallments(AMOUNT, TERM, RATE, RoundingMode.HALF_EVEN);

            assertEquals(4, loanOffers.size());
            loanOffers.forEach(offer -> {
                assertNotNull(offer);
                assertNotNull(offer.getTotalAmount());
                assertNotNull(offer.getMonthlyPayment());
                assertNotNull(offer.getRate());
            });
        }

        @Test
        void WhenDataForDenialIsGiven_ThenThrowClientDeniedException() {
            ScoringDataDto scoringDataDto = ScoringDataDto.builder()
                    .employment(EmploymentDto.builder()
                            .workingStatus(WorkingStatus.UNEMPLOYED)
                            .build())
                    .build();

            assertThrows(ClientRejected.class, () -> calculationService.calculateCredit(scoringDataDto));

            ScoringDataDto scoringDataDtoSecond = ScoringDataDto.builder()
                    .amount(AMOUNT)
                    .employment(EmploymentDto.builder()
                            .workingStatus(WorkingStatus.EMPLOYED)
                            .salary(BigDecimal.ONE)
                            .build())
                    .build();

            assertThrows(ClientRejected.class, () -> calculationService.calculateCredit(scoringDataDtoSecond));

            ScoringDataDto scoringDataDtoThird = ScoringDataDto.builder()
                    .amount(AMOUNT)
                    .birthdate(LocalDate.now().minusYears(90))
                    .employment(EmploymentDto.builder()
                            .workingStatus(WorkingStatus.EMPLOYED)
                            .salary(AMOUNT.multiply(BigDecimal.TEN))
                            .build())
                    .build();

            assertThrows(ClientRejected.class, () -> calculationService.calculateCredit(scoringDataDtoThird));

            ScoringDataDto scoringDataDtoFourth = ScoringDataDto.builder()
                    .amount(AMOUNT)
                    .birthdate(LocalDate.now().minusYears(35))
                    .employment(EmploymentDto.builder()
                            .workingStatus(WorkingStatus.EMPLOYED)
                            .salary(AMOUNT.multiply(BigDecimal.TEN))
                            .workExperienceTotal(1)
                            .build())
                    .build();

            assertThrows(ClientRejected.class, () -> calculationService.calculateCredit(scoringDataDtoFourth));

            ScoringDataDto scoringDataDtoFifth = ScoringDataDto.builder()
                    .amount(AMOUNT)
                    .birthdate(LocalDate.now().minusYears(35))
                    .employment(EmploymentDto.builder()
                            .workingStatus(WorkingStatus.EMPLOYED)
                            .salary(AMOUNT.multiply(BigDecimal.TEN))
                            .workExperienceTotal(100)
                            .workExperienceCurrent(1)
                            .build())
                    .build();

            assertThrows(ClientRejected.class, () -> calculationService.calculateCredit(scoringDataDtoFifth));
        }

        @Test
        void calculateCredit() {
            ScoringDataDto scoringDataDto = ScoringDataDto.builder()
                    .amount(AMOUNT)
                    .term(TERM)
                    .birthdate(LocalDate.now().minusYears(35))
                    .employment(EmploymentDto.builder()
                            .workingStatus(WorkingStatus.EMPLOYED)
                            .salary(AMOUNT.multiply(BigDecimal.TEN))
                            .workExperienceTotal(100)
                            .workExperienceCurrent(10)
                            .build())
                    .isInsuranceEnabled(false)
                    .isSalaryClient(false)
                    .build();

            Mockito.when(mockLoanCalculator.calculateAmount(Mockito.any(), Mockito.anyBoolean())).thenReturn(AMOUNT);
            Mockito.when(mockCheckRateService.calculateScoringRate(Mockito.any(ScoringDataDto.class))).thenReturn(RATE);
            Mockito.when(mockLoanCalculator.monthlyInstallments(Mockito.any(), Mockito.anyInt(), Mockito.any())).thenReturn(MONTHLY_PAYMENT);
            Mockito.when(mockLoanCalculator.calculatePSK(Mockito.any(), Mockito.anyInt())).thenReturn(Mockito.mock(BigDecimal.class));
            Mockito.when(mockLoanCalculator.calculatePaymentSchedule(Mockito.any(), Mockito.anyInt(), Mockito.any(), Mockito.any())).thenReturn(List.of(PaymentScheduleElementDto.builder().build()));

            CreditDto creditDto = calculationService.calculateCredit(scoringDataDto);

            assertNotNull(creditDto);

            Mockito.verify(mockCheckRateService).calculateScoringRate(scoringDataDto);
            Mockito.verify(mockLoanCalculator).calculateAmount(Mockito.eq(AMOUNT), Mockito.anyBoolean());
            Mockito.verify(mockLoanCalculator).monthlyInstallments(AMOUNT, TERM, RATE);
            Mockito.verify(mockLoanCalculator).calculatePSK(MONTHLY_PAYMENT, TERM);
            Mockito.verify(mockLoanCalculator).calculatePaymentSchedule(AMOUNT, TERM, MONTHLY_PAYMENT, RATE);

            assertNotNull(creditDto.getAmount());
            assertNotNull(creditDto.getRate());
            assertNotNull(creditDto.getTerm());
            assertNotNull(creditDto.getMonthlyPayment());
            assertNotNull(creditDto.getRate());
            assertNotNull(creditDto.getIsInsuranceEnabled());
            assertNotNull(creditDto.getIsSalaryClient());
            assertEquals(1, creditDto.getPaymentSchedule().size());
        }
}
