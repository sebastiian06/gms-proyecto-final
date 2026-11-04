package com.gms.billing.dto;

import com.gms.billing.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentDto(
        Long id,
        Long membershipId,
        BigDecimal amount,
        String method,
        LocalDate recordedDate
) {
    public static PaymentDto from(Payment p) {
        return new PaymentDto(
                p.getId(),
                p.getMembership().getId(),
                p.getAmount(),
                p.getMethod().name(),
                p.getRecordedDate()
        );
    }
}