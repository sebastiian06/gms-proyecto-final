package com.gms.billing.service;

import com.gms.billing.entity.Membership;
import com.gms.billing.repository.MembershipRepository;
import com.gms.members.dto.MemberDto;
import com.gms.members.service.MembersFacade;
import com.gms.plans.dto.PlanDto;
import com.gms.plans.service.PlansFacade;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final MembersFacade membersFacade;
    private final PlansFacade plansFacade;

    public MembershipService(MembershipRepository membershipRepository,
                             @Lazy MembersFacade membersFacade,
                             PlansFacade plansFacade) {
        this.membershipRepository = membershipRepository;
        this.membersFacade = membersFacade;
        this.plansFacade = plansFacade;
    }

    @Transactional
    public Membership create(Long memberId, Long planId, LocalDate startDate) {
        // Validate member exists and is active
        MemberDto member = membersFacade.findById(memberId);
        if (!member.active()) {
            throw new MemberInactiveException(memberId);
        }

        // Validate plan exists and is active
        PlanDto plan = plansFacade.findById(planId);
        if (!plan.active()) {
            throw new PlanInactiveException(planId);
        }

        // BR-01: only one active membership per member
        if (membershipRepository.existsByMemberIdAndStatus(memberId, Membership.Status.ACTIVE)) {
            throw new ActiveMembershipAlreadyExistsException(memberId);
        }

        // BR-02: lock the price at creation
        LocalDate endDate = startDate.plusDays(plan.durationDays());

        Membership membership = new Membership(memberId, planId, plan.price(), startDate, endDate);
        return membershipRepository.save(membership);
    }

    public static class MemberInactiveException extends RuntimeException {
        public MemberInactiveException(Long id) {
            super("Member " + id + " is inactive");
        }
    }

    public static class PlanInactiveException extends RuntimeException {
        public PlanInactiveException(Long id) {
            super("Plan " + id + " is inactive");
        }
    }

    public static class ActiveMembershipAlreadyExistsException extends RuntimeException {
        public ActiveMembershipAlreadyExistsException(Long memberId) {
            super("Member " + memberId + " already has an active membership");
        }
    }

    @Transactional(readOnly = true)
    public Page<Membership> findAll(Long memberIdFilter, Pageable pageable) {
        if (memberIdFilter == null) {
            return membershipRepository.findAll(pageable);
        }
        return membershipRepository.findByMemberId(memberIdFilter, pageable);
    }

    @Transactional(readOnly = true)
    public Membership findById(Long id) {
        return membershipRepository.findById(id)
                .orElseThrow(() -> new MembershipNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Optional<Membership> findActiveByMemberId(Long memberId) {
        return membershipRepository.findFirstByMemberIdAndStatusOrderByStartDateDesc(
                memberId, Membership.Status.ACTIVE);
    }

    public static class MembershipNotFoundException extends RuntimeException {
        public MembershipNotFoundException(Long id) {
            super("Membership with id " + id + " not found");
        }
    }

    @Transactional
    public void cancel(Long id) {
        Membership membership = membershipRepository.findById(id)
                .orElseThrow(() -> new MembershipNotFoundException(id));

        if (membership.getStatus() != Membership.Status.ACTIVE) {
            throw new MembershipNotActiveException(id, membership.getStatus());
        }

        membership.cancel();
        membershipRepository.save(membership);
    }

    public static class MembershipNotActiveException extends RuntimeException {
        public MembershipNotActiveException(Long id, Membership.Status status) {
            super("Membership " + id + " is " + status.name() + ", cannot be cancelled");
        }
    }
}