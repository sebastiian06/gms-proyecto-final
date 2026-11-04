package com.gms.reports.dto;

import java.math.BigDecimal;

public record ReportSummaryDto(
        long activeMembers,
        BigDecimal monthlyRevenue,
        long attendanceThisMonth,
        String mostSoldPlan
) {
}