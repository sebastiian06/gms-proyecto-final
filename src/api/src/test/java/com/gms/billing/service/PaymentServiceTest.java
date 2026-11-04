package com.gms.billing.service;

import com.gms.billing.entity.Membership;
import com.gms.billing.entity.Payment;
import com.gms.billing.repository.MembershipRepository;
import com.gms.billing.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Membership activeMembership;

    @BeforeEach
    void setUp() {
        activeMembership = new Membership(
                999L, 1L, new BigDecimal("50000.00"),
                LocalDate.now(), LocalDate.now().plusDays(30));
    }

    @Test
    void record_whenValidPayment_savesAndReturnsPayment() {
        // Given
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(activeMembership));
        when(paymentRepository.sumAmountByMembershipId(1L)).thenReturn(BigDecimal.ZERO);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Payment payment = paymentService.record(
                1L, new BigDecimal("30000.00"), Payment.Method.CASH, LocalDate.now());

        // Then
        assertThat(payment.getAmount()).isEqualByComparingTo(new BigDecimal("30000.00"));
        assertThat(payment.getMethod()).isEqualTo(Payment.Method.CASH);
        assertThat(payment.getMembership()).isEqualTo(activeMembership);

        // Verify that the payment was passed to save
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo(new BigDecimal("30000.00"));
    }

    @Test
    void record_whenPaymentExceedsLockedPrice_throwsPaymentExceedsBalanceException() {
        // Given: 30000 already paid, attempt 25000 more (would total 55000 > 50000)
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(activeMembership));
        when(paymentRepository.sumAmountByMembershipId(1L))
                .thenReturn(new BigDecimal("30000.00"));

        // When / Then
        assertThatThrownBy(() -> paymentService.record(
                1L, new BigDecimal("25000.00"), Payment.Method.CASH, LocalDate.now()))
                .isInstanceOf(PaymentService.PaymentExceedsBalanceException.class)
                .hasMessageContaining("exceeds remaining balance");

        // Verify that no payment was saved
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void record_whenMembershipNotActive_throwsMembershipNotActiveException() {
        // Given
        activeMembership.cancel();
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(activeMembership));

        // When / Then
        assertThatThrownBy(() -> paymentService.record(
                1L, new BigDecimal("10000.00"), Payment.Method.CASH, LocalDate.now()))
                .isInstanceOf(MembershipService.MembershipNotActiveException.class);

        verify(paymentRepository, never()).save(any(Payment.class));
    }
}