package com.gms.training.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateRoutineRequest(
        @NotBlank(message = "name must not be blank")
        @Size(min = 1, max = 200)
        String name
) {
}