package com.neoflex.calculator.dto;

import com.neoflex.calculator.enums.WorkingPosition;
import com.neoflex.calculator.enums.WorkingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class EmploymentDto {

    @NotNull(message = "Трудовой статус не может быть пустым")
    private WorkingStatus workingStatus;

    @Schema(type = "string", example = "1234567890")
    @NotBlank(message = "ИНН работодателя не может быть пустым")
    @Size(min = 10, max = 10, message = "ИНН должен состоять из 10 цифр")
    @Pattern(regexp = "^[0-9]*$", message = "ИНН должен состоять только из цифр")
    private String employerINN;

    @NotNull(message = "Зарплата не может быть пустой")
    @Positive(message = "Зарплата должна быть больше 0")
    private BigDecimal salary;

    @NotNull(message = "Должность не может быть пустой")
    private WorkingPosition workingPosition;

    @NotNull(message = "Общий трудовой стаж не может быть пустым")
    @Min(value = 0, message = "Общий трудовой стаж не может меньше 0")
    private Integer workExperienceTotal;

    @NotNull(message = "Трудовой стаж на текущем месте работы не может быть пустым")
    @Min(value = 0, message = "Стаж на текущем месте работы не может меньше 0")
    private Integer workExperienceCurrent;
}