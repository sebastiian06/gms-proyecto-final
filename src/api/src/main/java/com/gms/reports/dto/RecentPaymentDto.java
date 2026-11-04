package com.gms.reports.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecentPaymentDto(
        Long id,
        String memberName,
        String planName,
        BigDecimal amount,
        String method,
        LocalDate recordedDate
) {
}