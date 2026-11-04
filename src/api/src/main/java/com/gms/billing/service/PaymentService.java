
package com.gms.billing.service;

import com.gms.billing.entity.Membership;
import com.gms.billing.entity.Payment;
import com.gms.billing.repository.MembershipRepository;
import com.gms.billing.repository.PaymentRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MembershipRepository membershipRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            MembershipRepository membershipRepository) {
        this.paymentRepository = paymentRepository;
        this.membershipRepository = membershipRepository;
    }

    @Transactional
    public Payment record(
            Long membershipId,
            BigDecimal amount,
            Payment.Method method,
            LocalDate recordedDate) {

        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() ->
                        new MembershipService.MembershipNotFoundException(membershipId));

        if (membership.getStatus() != Membership.Status.ACTIVE) {
            throw new MembershipService.MembershipNotActiveException(
                    membershipId, membership.getStatus());
        }

        BigDecimal alreadyPaid =
                paymentRepository.sumAmountByMembershipId(membershipId);

        BigDecimal newTotal = alreadyPaid.add(amount);

        // BR-06: cannot exceed lockedPrice
        if (newTotal.compareTo(membership.getLockedPrice()) > 0) {
            throw new PaymentExceedsBalanceException(
                    membershipId,
                    membership.getLockedPrice(),
                    alreadyPaid,
                    amount
            );
        }

        Payment payment = new Payment(
                membership,
                amount,
                method,
                recordedDate
        );

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment update(
            Long id,
            BigDecimal amount,
            Payment.Method method) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Payment not found"));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero");
        }

        if (method == null) {
            throw new IllegalArgumentException(
                    "Payment method is required");
        }

        Membership membership = payment.getMembership();

        BigDecimal alreadyPaid =
                paymentRepository.sumAmountByMembershipId(membership.getId());

        // Excluimos el pago actual antes de calcular el nuevo total.
        BigDecimal totalWithoutCurrentPayment =
                alreadyPaid.subtract(payment.getAmount());

        BigDecimal newTotal =
                totalWithoutCurrentPayment.add(amount);

        if (newTotal.compareTo(membership.getLockedPrice()) > 0) {
            throw new PaymentExceedsBalanceException(
                    membership.getId(),
                    membership.getLockedPrice(),
                    totalWithoutCurrentPayment,
                    amount
            );
        }

        payment.setAmount(amount);
        payment.setMethod(method);

        return paymentRepository.save(payment);
    }

    @Transactional
    public void delete(Long id) {

        if (!paymentRepository.existsById(id)) {
            throw new RuntimeException("Payment not found");
        }

        paymentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Payment> findByMembershipId(Long membershipId) {

        if (!membershipRepository.existsById(membershipId)) {
            throw new MembershipService.MembershipNotFoundException(membershipId);
        }

        return paymentRepository.findByMembershipId(membershipId);
    }

    @Transactional(readOnly = true)
    public List<Payment> findByMemberId(Long memberId) {

        // Recuperamos todas las membresías, sin límite de paginación.
        List<Long> membershipIds = membershipRepository
                .findByMemberId(memberId, Pageable.unpaged())
                .stream()
                .map(Membership::getId)
                .toList();

        if (membershipIds.isEmpty()) {
            return List.of();
        }

        // Obtenemos los pagos de todas sus membresías.
        return paymentRepository.findAll()
                .stream()
                .filter(payment ->
                        membershipIds.contains(payment.getMembership().getId()))
                .sorted((a, b) ->
                        b.getRecordedDate().compareTo(a.getRecordedDate()))
                .toList();
    }

    public static class PaymentExceedsBalanceException extends RuntimeException {

        public PaymentExceedsBalanceException(
                Long membershipId,
                BigDecimal lockedPrice,
                BigDecimal alreadyPaid,
                BigDecimal attempted) {

            super(String.format(
                    "Payment of %.2f exceeds remaining balance for membership %d " +
                            "(locked: %.2f, already paid: %.2f)",
                    attempted,
                    membershipId,
                    lockedPrice,
                    alreadyPaid
            ));
        }
    }
}
