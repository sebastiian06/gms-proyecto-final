
package com.gms.billing.controller;

import com.gms.billing.dto.CreateMembershipRequest;
import com.gms.billing.dto.CreatePaymentRequest;
import com.gms.billing.dto.MembershipDto;
import com.gms.billing.dto.PaymentDto;
import com.gms.billing.service.BillingFacade;
import com.gms.billing.service.PaymentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/memberships")
public class MembershipController {

    private final BillingFacade billingFacade;
    private final PaymentService paymentService;

    public MembershipController(
            BillingFacade billingFacade,
            PaymentService paymentService) {
        this.billingFacade = billingFacade;
        this.paymentService = paymentService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @PostMapping
    public ResponseEntity<MembershipDto> createMembership(
            @Valid @RequestBody CreateMembershipRequest request) {

        MembershipDto membership = billingFacade.createMembership(
                request.memberId(), request.planId(), request.startDate());

        return ResponseEntity.status(HttpStatus.CREATED).body(membership);
    }

    @GetMapping
    public ResponseEntity<Page<MembershipDto>> listMemberships(
            @RequestParam(required = false) Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(
                page,
                Math.min(size, 100),
                Sort.by("startDate").descending()
        );

        return ResponseEntity.ok(billingFacade.findAll(memberId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MembershipDto> getMembership(@PathVariable Long id) {
        return ResponseEntity.ok(billingFacade.findById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelMembership(@PathVariable Long id) {
        billingFacade.cancelMembership(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @PostMapping("/{id}/payments")
    public ResponseEntity<PaymentDto> recordPayment(
            @PathVariable Long id,
            @Valid @RequestBody CreatePaymentRequest request) {

        PaymentDto payment = billingFacade.recordPayment(
                id,
                request.amount(),
                request.method(),
                request.recordedDate()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @GetMapping("/{id}/payments")
    public ResponseEntity<List<PaymentDto>> listPayments(@PathVariable Long id) {
        return ResponseEntity.ok(billingFacade.listPayments(id));
    }

    // Historial completo de pagos de un miembro,
    // incluyendo pagos de membresías anteriores.
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @GetMapping("/members/{memberId}/payments")
    public ResponseEntity<List<PaymentDto>> getMemberPaymentHistory(
            @PathVariable Long memberId) {

        List<PaymentDto> payments = paymentService.findByMemberId(memberId)
                .stream()
                .map(PaymentDto::from)
                .toList();

        return ResponseEntity.ok(payments);
    }
}
