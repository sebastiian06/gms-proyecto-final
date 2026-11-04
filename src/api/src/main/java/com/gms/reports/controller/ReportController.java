package com.gms.reports.controller;

import com.gms.reports.dto.RecentPaymentDto;
import com.gms.reports.dto.ReportSummaryDto;
import com.gms.reports.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @GetMapping("/summary")
    public ResponseEntity<ReportSummaryDto> summary() {
        return ResponseEntity.ok(reportService.summary());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @GetMapping("/recent-payments")
    public ResponseEntity<List<RecentPaymentDto>> recentPayments(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(reportService.recentPayments(limit));
    }
}