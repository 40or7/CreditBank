package com.neoflex.calculator.service;

import com.neoflex.calculator.CalculatorApplication;
import com.neoflex.calculator.dto.PaymentScheduleElementDto;
import com.neoflex.calculator.util.CalculatorUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


//@TestPropertySource(locations = "classpath:application-test.yml")
@Profile("test")
@SpringBootTest
//@RunWith(SpringRunner.class)
public class LoanCalculatorTest {

        private final BigDecimal INSURANCE_COEFFICIENT = BigDecimal.valueOf(1.1);

        private final BigDecimal RATE_15_000 = BigDecimal.valueOf(15);

        private final BigDecimal RATE_5_000_000 = BigDecimal.valueOf(6);

        private final BigDecimal AMOUNT_15_000 = BigDecimal.valueOf(15_000);

        private final BigDecimal AMOUNT_5_000_000 = BigDecimal.valueOf(5_000_000);

        private final BigDecimal MONTHLY_PAY_FOR_15_000 = BigDecimal.valueOf(664.2861963);

        private final BigDecimal MONTHLY_PAY_FOR_5_000_000 = BigDecimal.valueOf(22706.93845);

        @Autowired
        LoanCalculator loanCalculator;

        @Test
        void monthlyInstallments() {
            BigDecimal monthlyPaymentFirst = loanCalculator.monthlyInstallments(AMOUNT_15_000, 36, RATE_15_000)
                    .setScale(2, RoundingMode.HALF_EVEN);
            testBigDecimal(MONTHLY_PAY_FOR_15_000.setScale(2, RoundingMode.HALF_EVEN), monthlyPaymentFirst);

            BigDecimal monthlyPaymentSecond = loanCalculator.monthlyInstallments(BigDecimal.valueOf(5_000_000), 600, RATE_5_000_000)
                    .setScale(2, RoundingMode.HALF_EVEN);
            testBigDecimal(MONTHLY_PAY_FOR_5_000_000.setScale(2,RoundingMode.HALF_EVEN), monthlyPaymentSecond);
        }

        private void testBigDecimal(BigDecimal expected, BigDecimal monthlyPayment) {
            assertNotNull(monthlyPayment);
            assertEquals(expected, monthlyPayment);
        }

        @Test
        void calculateAmount() {
            BigDecimal amountFirst = loanCalculator.calculateAmount(AMOUNT_15_000, true);
            testBigDecimal(AMOUNT_15_000.multiply(INSURANCE_COEFFICIENT).setScale(2, RoundingMode.HALF_EVEN), amountFirst);

            BigDecimal amountSecond = loanCalculator.calculateAmount(AMOUNT_5_000_000, true);
            testBigDecimal(AMOUNT_5_000_000.multiply(INSURANCE_COEFFICIENT).setScale(2, RoundingMode.HALF_EVEN), amountSecond);
        }

        @Test
        void calculatePSK() {
            BigDecimal pskFirst = loanCalculator.calculatePSK(MONTHLY_PAY_FOR_15_000, 36);
            testBigDecimal(BigDecimal.valueOf(23914.30).setScale(2, RoundingMode.HALF_EVEN), pskFirst);

            BigDecimal pskSecond = loanCalculator.calculatePSK(MONTHLY_PAY_FOR_5_000_000, 600);
            testBigDecimal(BigDecimal.valueOf(13624163.07).setScale(2), pskSecond);
        }

        @Test
        void calculatePaymentSchedule() {
            List<PaymentScheduleElementDto> paymentScheduleElementDto = loanCalculator.calculatePaymentSchedule(AMOUNT_15_000, 36, MONTHLY_PAY_FOR_15_000, RATE_15_000);
            BigDecimal monthlyRate = CalculatorUtils.getMonthlyRate(RATE_15_000);
            AtomicReference<BigDecimal> amount = new AtomicReference<>(AMOUNT_15_000);
            paymentScheduleElementDto.forEach(element -> {
                assertNotNull(element);
                assertEquals(amount.get().multiply(monthlyRate).setScale(2, RoundingMode.HALF_EVEN), element.getInterestPayment());
                amount.set(element.getRemainingDebt());});
        }
    }
