package com.gms.billing.dto;

import com.gms.billing.entity.Payment;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreatePaymentRequest(
        @NotNull(message = "amount must not be null")
        @DecimalMin(value = "0.01", message = "amount must be > 0")
        BigDecimal amount,

        @NotNull(message = "method must not be null")
        Payment.Method method,

        @NotNull(message = "recordedDate must not be null")
        LocalDate recordedDate
) {
}