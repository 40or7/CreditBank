package com.neoflex.calculator.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.neoflex.calculator.enums.FamilyStatus;
import com.neoflex.calculator.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ScoringDataDto {
    @NotNull(message = "Поле должно быть заполнено")
    @DecimalMin(value = "30000", message = "Сумма должна быть больше или равна 30000")
    private BigDecimal amount;

    @NotNull(message = "Поле должно быть заполнено")
    @Min(value = 6, message = "Срок кредита должен быть от 6 месяцев ")
    private Integer term;


    private String firstName;
    private String lastName;
    private String middleName;

    @Schema(type = "string", example = "FEMALE")
    @NotBlank(message = "Поле должно быть заполнено")
    private Gender gender;

    @Schema(type = "string", example = "2008.01.01")
    @NotNull(message = "Поле должно быть заполнено")
    @JsonFormat(pattern = "yyyy.MM.dd")
    private LocalDate birthdate;

    @NotBlank(message = "Поле должно быть заполнено")
    @Size(min = 4, max = 4, message = "Серия паспорта должна состоять из 4 цифр")
    @Pattern(regexp = "^[0-9]*$", message = "Серия паспорта должна состоять только из цифр")
    private String passportSeries;

    @NotBlank(message = "Поле должно быть заполнено")
    @Size(min = 6, max = 6, message = "Номер паспорта должен состоять из 6 цифр")
    @Pattern(regexp = "^[0-9]*$", message = "Номер паспорта должен состоять только из цифр")
    private String passportNumber;

    @Schema(type = "string", example = "2008.01.01")
    @NotNull(message = "Дата окончания срока действия должна быть заполнена")
    @Future(message = "Паспорт  должен быть действителен")
    @JsonFormat(pattern="yyyy.MM.dd")
    private LocalDate passportIssueDate;

    @NotBlank(message = "Поле должно быть заполнено")
    private String passportIssueBranch;

    @NotBlank(message = "Поле должно быть заполнено")
    private FamilyStatus familyStatus;

    @Min(value = 0, message = "Детей не может быть меньше 0")
    private Integer dependentAmount;

    @NotNull(message = "Поле должно быть заполнено")
    private EmploymentDto employment;

    @NotNull(message = "Поле должно быть заполнено")
    private String accountNumber;

    @NotNull(message = "Поле должно быть заполнено")
    private Boolean isInsuranceEnabled;

    @NotNull(message = "Поле статус зарплатного клиента должно быть заполнено ")
    private Boolean isSalaryClient;
}