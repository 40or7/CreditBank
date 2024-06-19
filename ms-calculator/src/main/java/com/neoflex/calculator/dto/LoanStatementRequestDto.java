package com.neoflex.calculator.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/*
 *LoanStatementRequestDto
 *
 * @author Shilin Vyacheslav
 */
@Data
@Builder
public class LoanStatementRequestDto{
    @NotNull(message = "Поле должно быть заполнено")
    @DecimalMin(value = "30000", message = "Сумма должна быть больше или равна 30000")
    private BigDecimal amount;

    @NotNull(message = "Поле должно быть заполнено")
    @Min(value = 6, message = "Срок кредита должен быть от 6 месяцев ")
    private Integer term;

    @Schema(type = "string", example = "John")
    @NotBlank(message = "Поле должно быть заполнено")
    @Size(min = 2, max = 30, message = "Имя должно содержать от 2 до 30 символов.")
    @Pattern(regexp = "^[a-zA-Z]+", message = "Имя должно состоять из латинских букв")
    private String firstName;

    @Schema(type = "string", example = "Doe")
    @NotBlank(message = "Поле должно быть заполнено")
    @Size(min = 2, max = 30, message = "Фамилия должна состоять от 2 до 30 символов .")
    @Pattern(regexp = "^[a-zA-Z]+", message = "Фамилия должна состоять из латинских букв")
    private String lastName;

    @Size(min = 2, max = 30, message = "Отчество должно быть от 2 до 30 символов длиной.")
    private String middleName;

    @Schema(type = "string", example = "user@mail.com")
    @NotBlank(message = "Поле должно быть заполнено")
    @Email(regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$", message = "Электронная почта должна соответствовать общепринятому формату")
    private String email;

    @Schema(type = "string", example = "2006.01.01")
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
}