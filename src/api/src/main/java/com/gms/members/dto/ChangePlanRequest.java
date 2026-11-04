package com.gms.members.dto;

import jakarta.validation.constraints.NotNull;

public record ChangePlanRequest(
        @NotNull(message = "planId must not be null")
        Long planId
) {
}