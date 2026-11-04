package com.gms.training.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateExerciseRequest(
        @NotBlank(message = "name must not be blank")
        @Size(min = 1, max = 200)
        String name,

        @NotNull(message = "sets must not be null")
        @Min(value = 1, message = "sets must be >= 1")
        Integer sets,

        @NotNull(message = "repetitions must not be null")
        @Min(value = 1, message = "repetitions must be >= 1")
        Integer repetitions
) {
}