package com.gms.billing.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateMembershipRequest(
        @NotNull(message = "memberId must not be null")
        Long memberId,

        @NotNull(message = "planId must not be null")
        Long planId,

        @NotNull(message = "startDate must not be null")
        LocalDate startDate
) {
}