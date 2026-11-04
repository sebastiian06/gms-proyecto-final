package com.gms.billing.service;

import com.gms.billing.dto.MembershipDto;
import com.gms.billing.dto.PaymentDto;
import com.gms.billing.entity.Membership;
import com.gms.billing.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class BillingFacade {

    private final MembershipService membershipService;
    private final PaymentService paymentService;

    public BillingFacade(MembershipService membershipService, PaymentService paymentService) {
        this.membershipService = membershipService;
        this.paymentService = paymentService;
    }

    public MembershipDto createMembership(Long memberId, Long planId, LocalDate startDate) {
        Membership membership = membershipService.create(memberId, planId, startDate);
        return MembershipDto.from(membership);
    }

    public Page<MembershipDto> findAll(Long memberIdFilter, Pageable pageable) {
        return membershipService.findAll(memberIdFilter, pageable).map(MembershipDto::from);
    }

    public MembershipDto findById(Long id) {
        return MembershipDto.from(membershipService.findById(id));
    }

    public void cancelMembership(Long id) {
        membershipService.cancel(id);
    }

    public PaymentDto recordPayment(Long membershipId, BigDecimal amount,
                                    Payment.Method method, LocalDate recordedDate) {
        Payment payment = paymentService.record(membershipId, amount, method, recordedDate);
        return PaymentDto.from(payment);
    }

    public List<PaymentDto> listPayments(Long membershipId) {
        return paymentService.findByMembershipId(membershipId)
                .stream()
                .map(PaymentDto::from)
                .collect(Collectors.toList());
    }
}