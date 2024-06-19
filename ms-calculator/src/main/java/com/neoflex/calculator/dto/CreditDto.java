package com.neoflex.calculator.dto;

import lombok.Builder;
import lombok.Data;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/*
 *CreditDto
 *
 * @author Shilin Vyacheslav
 */
@Data
@Builder
public class CreditDto{
    private BigDecimal amount;
    private Integer term;
    private BigDecimal monthlyPayment;
    private BigDecimal rate;
    private BigDecimal psk;
    private Boolean isInsuranceEnabled;
    private Boolean isSalaryClient;
    private List<PaymentScheduleElementDto> paymentSchedule;
}