package com.gms.training.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTrainerRequest(
        @NotBlank(message = "username must not be blank")
        @Size(min = 3, max = 100)
        String username,

        @NotBlank(message = "password must not be blank")
        @Size(min = 8, max = 100)
        String password,

        @NotBlank(message = "fullName must not be blank")
        @Size(min = 1, max = 200)
        String fullName
) {
}