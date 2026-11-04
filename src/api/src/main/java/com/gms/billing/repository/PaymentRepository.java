package com.gms.billing.repository;

import com.gms.billing.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByMembershipId(Long membershipId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.membership.id = :membershipId")
    BigDecimal sumAmountByMembershipId(@Param("membershipId") Long membershipId);
}