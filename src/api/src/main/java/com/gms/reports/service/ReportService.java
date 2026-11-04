package com.gms.reports.service;

import com.gms.attendance.service.AttendanceFacade;
import com.gms.billing.entity.Membership;
import com.gms.billing.entity.Payment;
import com.gms.billing.repository.MembershipRepository;
import com.gms.billing.repository.PaymentRepository;
import com.gms.members.entity.Member;
import com.gms.members.repository.MemberRepository;
import com.gms.plans.entity.Plan;
import com.gms.plans.repository.PlanRepository;
import com.gms.reports.dto.RecentPaymentDto;
import com.gms.reports.dto.ReportSummaryDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;
    private final MembershipRepository membershipRepository;
    private final PlanRepository planRepository;
    private final AttendanceFacade attendanceFacade;

    public ReportService(MemberRepository memberRepository,
                         PaymentRepository paymentRepository,
                         MembershipRepository membershipRepository,
                         PlanRepository planRepository,
                         AttendanceFacade attendanceFacade) {
        this.memberRepository = memberRepository;
        this.paymentRepository = paymentRepository;
        this.membershipRepository = membershipRepository;
        this.planRepository = planRepository;
        this.attendanceFacade = attendanceFacade;
    }

    public ReportSummaryDto summary() {
        long activeMembers = memberRepository.findAll().stream()
                .filter(Member::getActive).count();

        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        List<Payment> monthPayments = paymentRepository.findAll().stream()
                .filter(p -> !p.getRecordedDate().isBefore(startOfMonth)
                        && !p.getRecordedDate().isAfter(endOfMonth))
                .toList();

        BigDecimal monthlyRevenue = monthPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long attendanceThisMonth = attendanceFacade.countThisMonth();

        String mostSoldPlan = findMostSoldPlan(startOfMonth, endOfMonth);

        return new ReportSummaryDto(
                activeMembers, monthlyRevenue, attendanceThisMonth, mostSoldPlan);
    }

    private String findMostSoldPlan(LocalDate from, LocalDate to) {
        List<Membership> memberships = membershipRepository.findAll().stream()
                .filter(m -> !m.getStartDate().isBefore(from) && !m.getStartDate().isAfter(to))
                .toList();

        if (memberships.isEmpty()) {
            return "—";
        }

        var counts = memberships.stream()
                .collect(Collectors.groupingBy(Membership::getPlanId, Collectors.counting()));

        Long mostSoldPlanId = counts.entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse(null);

        if (mostSoldPlanId == null) return "—";

        return planRepository.findById(mostSoldPlanId)
                .map(Plan::getName)
                .orElse("—");
    }

    public List<RecentPaymentDto> recentPayments(int limit) {
        var page = PageRequest.of(0, Math.min(limit, 20),
                Sort.by("recordedDate").descending());

        return paymentRepository.findAll(page).getContent().stream()
                .map(p -> {
                    Long memberId = p.getMembership().getMemberId();
                    Long planId = p.getMembership().getPlanId();
                    String memberName = memberRepository.findById(memberId)
                            .map(Member::getFullName).orElse("Unknown");
                    String planName = planRepository.findById(planId)
                            .map(Plan::getName).orElse("Unknown");
                    return new RecentPaymentDto(
                            p.getId(),
                            memberName,
                            planName,
                            p.getAmount(),
                            p.getMethod().name(),
                            p.getRecordedDate()
                    );
                })
                .toList();
    }
}