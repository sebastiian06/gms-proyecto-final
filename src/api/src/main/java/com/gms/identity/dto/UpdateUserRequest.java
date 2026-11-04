package com.gms.identity.dto;

import com.gms.identity.entity.Role;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(min = 3, max = 100) String username,
        @Size(min = 8, max = 100) String password,
        Role role
) {
}