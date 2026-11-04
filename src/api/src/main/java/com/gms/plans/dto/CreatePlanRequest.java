package com.gms.plans.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreatePlanRequest(
        @NotBlank(message = "name must not be blank")
        @Size(min = 1, max = 100, message = "name must be between 1 and 100 characters")
        String name,

        @NotNull(message = "price must not be null")
        @DecimalMin(value = "0.0", message = "price must be >= 0")
        BigDecimal price,

        @NotNull(message = "durationDays must not be null")
        @Min(value = 1, message = "durationDays must be >= 1")
        Integer durationDays
) {
}