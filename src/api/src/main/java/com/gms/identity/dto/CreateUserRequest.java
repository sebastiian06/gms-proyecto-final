package com.gms.identity.dto;

import com.gms.identity.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "username must not be blank")
        @Size(min = 3, max = 100, message = "username must be between 3 and 100 characters")
        String username,

        @NotBlank(message = "password must not be blank")
        @Size(min = 8, max = 100, message = "password must be between 8 and 100 characters")
        String password,

        @NotNull(message = "role must not be null")
        Role role,

        @Size(max = 200, message = "fullName must be at most 200 characters")
        String fullName
) {
}