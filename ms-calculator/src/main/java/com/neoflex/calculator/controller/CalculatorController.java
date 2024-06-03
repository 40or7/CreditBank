package com.neoflex.calculator.controller;

import com.neoflex.calculator.dto.CreditDto;
import com.neoflex.calculator.dto.LoanOfferDto;
import com.neoflex.calculator.dto.LoanStatementRequestDto;
import com.neoflex.calculator.dto.ScoringDataDto;
import com.neoflex.calculator.service.CalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("calculator")
@Slf4j
public class CalculatorController {

    private final CalculationService calculationService;

    @Operation(summary = "Выполняет прескоринг и отправляет четыре кредитных предложения")
    @ApiResponses(value = {
            @ApiResponse(description = "Успешный ответ на запрос с четырьмя кредитными предложениями", responseCode = "200"),
            @ApiResponse(description = "Ошибки при валидации", responseCode = "400", content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(schema = @Schema(implementation = ErrorResponse.class))})
    })
    @PostMapping(value = "offers", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<LoanOfferDto> sendOffers(@Valid @RequestBody LoanStatementRequestDto loanStatementRequestDto) {
        log.info("Sending offer request: {}", loanStatementRequestDto);
        return calculationService.generateLoanOffers(loanStatementRequestDto);
    }

    @Operation(summary = "Рассчитывает кредит и отправляет график платежей")
    @ApiResponses(value = {
            @ApiResponse(description = "Успешный ответ на запрос с характеристиками кредита и графиком платежей", responseCode = "200"),
            @ApiResponse(description = "Ошибки при валидации или отказ в займе", responseCode = "400", content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(schema = @Schema(implementation = ErrorResponse.class))})
    })
    @PostMapping(value = "calc", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public CreditDto sendCredit(@Valid @RequestBody ScoringDataDto scoringDataDto) {
        log.info("Sending credit request: {}", scoringDataDto);
        return calculationService.calculateCredit(scoringDataDto);
    }
}