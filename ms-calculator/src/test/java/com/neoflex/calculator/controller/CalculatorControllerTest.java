package com.neoflex.calculator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neoflex.calculator.dto.CreditDto;
import com.neoflex.calculator.dto.LoanStatementRequestDto;
import com.neoflex.calculator.dto.ScoringDataDto;
import com.neoflex.calculator.exception.ClientRejected;
import com.neoflex.calculator.service.CalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CalculatorController.class)
class CalculatorControllerTest{

    private final Map<String, Object> loanOfferRequestMap = new HashMap<>();

    private final Map <String, Object> scoringDataRequest = new HashMap<>();

    private final Map <String, Object> employment = new HashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CalculationService calculationService;

    @BeforeEach
    void setUp() {
        loanOfferRequestMap.put("amount", "250000");
        loanOfferRequestMap.put("term", "12");
        loanOfferRequestMap.put("firstName", "Lil");
        loanOfferRequestMap.put("lastName", "Rogu");
        loanOfferRequestMap.put("email", "Lil.rogu@mail.com");
        loanOfferRequestMap.put("birthdate", "1998.05.01");
        loanOfferRequestMap.put("passportSeries", "1234");
        loanOfferRequestMap.put("passportNumber", "123456");

        employment.put("employmentStatus", "EMPLOYED");
        employment.put("employerINN", "1234567890");
        employment.put("salary", 150000);
        employment.put("position", "TOP_MANAGER");
        employment.put("workExperienceTotal", 360);
        employment.put("workExperienceCurrent", 36);

        scoringDataRequest.putAll(loanOfferRequestMap);
        scoringDataRequest.remove("email");
        scoringDataRequest.put("gender", "male");
        scoringDataRequest.put("passportIssueDate", "2021.05.25");
        scoringDataRequest.put("passportIssueBranch", "test");
        scoringDataRequest.put("maritalStatus", "MARRIED");
        scoringDataRequest.put("employment", employment);
        scoringDataRequest.put("accountNumber", "123456");
        scoringDataRequest.put("isInsuranceEnabled", false);
        scoringDataRequest.put("isSalaryClient", false);
    }
    @Test
    void sendOffers() throws Exception {
        String json = mapper.writeValueAsString(loanOfferRequestMap);

        Mockito.when(calculationService.generateLoanOffers(Mockito.any(LoanStatementRequestDto.class))).thenReturn(List.of());

        mockMvc.perform(post("/calculator/offers").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk());
    }

    @Test
    void IncorrectlyFilledDataBadRequest() throws Exception {
        loanOfferRequestMap.replace("amount", "25000");
        loanOfferRequestMap.replace("firstName", "111");
        loanOfferRequestMap.replace("email", "@mail.com");
        String json = mapper.writeValueAsString(loanOfferRequestMap);

        mockMvc.perform(post("/calculator/offers").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void sendCredit() throws Exception {
        String json = mapper.writeValueAsString(scoringDataRequest);

        Mockito.when(calculationService.calculateCredit(Mockito.any(ScoringDataDto.class))).thenReturn(CreditDto.builder()
                .paymentSchedule(List.of())
                .build());

        mockMvc.perform(post("/calculator/calc").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk());
    }

    @Test
    void CreditInformationIsIncorrectBadRequest() throws Exception {
        scoringDataRequest.replace("amount", "0");
        String json = mapper.writeValueAsString(scoringDataRequest);

        mockMvc.perform(post("/calculator/calc").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void LoanIsRejectedBadRequest() throws Exception {
        scoringDataRequest.replace("employmentStatus", "UNEMPLOYED");
        String json = mapper.writeValueAsString(scoringDataRequest);

        Mockito.when(calculationService.calculateCredit(Mockito.any(ScoringDataDto.class))).thenThrow(new ClientRejected("В кредите отказано"));

        mockMvc.perform(post("/calculator/calc").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());

    }

    @Test
    void AgeLessThan18BadRequest() throws Exception {
        scoringDataRequest.replace("birthdate", "2020.01.01");
        String json = mapper.writeValueAsString(scoringDataRequest);

        mockMvc.perform(post("/calculator/calc").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }
}