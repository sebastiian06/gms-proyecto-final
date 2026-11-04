
package com.gms.billing.controller;

import com.gms.billing.dto.PaymentDto;
import com.gms.billing.entity.Payment;
import com.gms.billing.service.PaymentService;
import com.gms.identity.service.JwtAuthFilter;
import com.gms.members.dto.MemberDto;
import com.gms.members.service.MembersFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final MembersFacade membersFacade;

    public PaymentController(
            PaymentService paymentService,
            MembersFacade membersFacade) {
        this.paymentService = paymentService;
        this.membersFacade = membersFacade;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PaymentDto> updatePayment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        BigDecimal amount = new BigDecimal(
                body.get("amount").toString()
        );

        Payment.Method method = Payment.Method.valueOf(
                body.get("method").toString()
        );

        Payment payment = paymentService.update(id, amount, method);

        return ResponseEntity.ok(PaymentDto.from(payment));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(
            @PathVariable Long id) {

        paymentService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'MEMBER')")
    @GetMapping("/members/{memberId}")
    public ResponseEntity<List<PaymentDto>> getMemberPayments(
            @PathVariable Long memberId,
            Authentication authentication) {

        // Los miembros solo pueden consultar su propio historial.
        boolean isMember = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_MEMBER")
                );

        if (isMember) {

            JwtAuthFilter.AuthenticatedUser user =
                    (JwtAuthFilter.AuthenticatedUser)
                            authentication.getPrincipal();

            MemberDto currentMember =
                    membersFacade.findByUsername(user.username());

            if (!currentMember.id().equals(memberId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        // ADMIN y RECEPTIONIST pueden consultar cualquier miembro.
        List<PaymentDto> payments = paymentService.findByMemberId(memberId)
                .stream()
                .map(PaymentDto::from)
                .toList();

        return ResponseEntity.ok(payments);
    }
}
