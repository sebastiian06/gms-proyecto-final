package com.gms.members.dto;

import com.gms.billing.entity.Payment;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateMemberRequest(
        @NotBlank(message = "username must not be blank")
        @Size(min = 3, max = 100)
        String username,

        @NotBlank(message = "password must not be blank")
        @Size(min = 8, max = 100)
        String password,

        @NotBlank(message = "documentNumber must not be blank")
        @Size(min = 1, max = 50)
        String documentNumber,

        @NotBlank(message = "fullName must not be blank")
        @Size(min = 1, max = 200)
        String fullName,

        @NotBlank(message = "contact must not be blank")
        @Size(min = 1, max = 200)
        String contact,

        @NotNull(message = "planId must not be null")
        Long planId,

        @NotNull(message = "initialPaymentMethod must not be null")
        Payment.Method initialPaymentMethod,

        @DecimalMin(value = "0.01", message = "initialPaymentAmount must be > 0")
        BigDecimal initialPaymentAmount,

        Long trainerId
) {
}