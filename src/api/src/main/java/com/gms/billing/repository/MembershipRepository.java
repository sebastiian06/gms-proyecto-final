package com.gms.billing.repository;

import com.gms.billing.entity.Membership;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    boolean existsByMemberIdAndStatus(Long memberId, Membership.Status status);

    Page<Membership> findByMemberId(Long memberId, Pageable pageable);

    Optional<Membership> findFirstByMemberIdAndStatusOrderByStartDateDesc(
            Long memberId, Membership.Status status);
}