
package com.gms.members.service;

import com.gms.billing.entity.Membership;
import com.gms.billing.entity.Payment;
import com.gms.billing.service.MembershipService;
import com.gms.billing.service.PaymentService;
import com.gms.identity.dto.UserDto;
import com.gms.identity.entity.Role;
import com.gms.identity.service.IdentityFacade;
import com.gms.members.dto.CreateMemberRequest;
import com.gms.members.dto.UpdateMemberRequest;
import com.gms.members.entity.Member;
import com.gms.members.repository.MemberRepository;
import com.gms.training.entity.TrainerMember;
import com.gms.training.repository.TrainerMemberRepository;
import com.gms.training.service.TrainerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@Service
public class MemberService {

    public record TrainerInfo(Long id, String name) {
    }

    private final MemberRepository memberRepository;
    private final IdentityFacade identityFacade;
    private final MembershipService membershipService;
    private final PaymentService paymentService;
    private final TrainerMemberRepository trainerMemberRepository;
    private final TrainerService trainerService;

    public MemberService(
            MemberRepository memberRepository,
            IdentityFacade identityFacade,
            MembershipService membershipService,
            PaymentService paymentService,
            TrainerMemberRepository trainerMemberRepository,
            TrainerService trainerService) {

        this.memberRepository = memberRepository;
        this.identityFacade = identityFacade;
        this.membershipService = membershipService;
        this.paymentService = paymentService;
        this.trainerMemberRepository = trainerMemberRepository;
        this.trainerService = trainerService;
    }

    @Transactional
    public Member registerMember(CreateMemberRequest request) {
        if (memberRepository.existsByDocumentNumber(request.documentNumber())) {
            throw new DuplicateDocumentNumberException(request.documentNumber());
        }

        UserDto user = identityFacade.createUser(
                request.username(),
                request.password(),
                Role.MEMBER
        );

        if (memberRepository.existsByUserId(user.id())) {
            throw new IllegalStateException(
                    "User already has a member profile: " + user.id()
            );
        }

        Member member = new Member(
                user.id(),
                request.documentNumber(),
                request.fullName(),
                request.contact()
        );

        member = memberRepository.save(member);

        identityFacade.generateAccessCode(user.id());

        Membership membership = membershipService.create(
                member.getId(),
                request.planId(),
                LocalDate.now()
        );

        BigDecimal paymentAmount = request.initialPaymentAmount() != null
                ? request.initialPaymentAmount()
                : membership.getLockedPrice();

        paymentService.record(
                membership.getId(),
                paymentAmount,
                request.initialPaymentMethod(),
                LocalDate.now()
        );

        if (request.trainerId() != null) {
            trainerMemberRepository.save(
                    new TrainerMember(
                            trainerService.findById(request.trainerId()),
                            member.getId()
                    )
            );
        }

        return member;
    }

    public static class DuplicateDocumentNumberException
            extends RuntimeException {

        public DuplicateDocumentNumberException(String documentNumber) {
            super("Document number already exists: " + documentNumber);
        }
    }

    @Transactional
    public Member updateProfile(Long id, String fullName, String contact) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));

        if (fullName != null) {
            member.setFullName(fullName);
        }

        if (contact != null) {
            member.setContact(contact);
        }

        return memberRepository.save(member);
    }

    public static class MemberNotFoundException extends RuntimeException {

        public MemberNotFoundException(Long id) {
            super("Member with id " + id + " not found");
        }

        public MemberNotFoundException(String message) {
            super(message);
        }
    }

    @Transactional
    public void deactivate(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));

        if (!member.getActive()) {
            throw new AlreadyInactiveException(id);
        }

        member.deactivate();
        memberRepository.save(member);
    }

    @Transactional
    public void activate(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));

        if (member.getActive()) {
            throw new MemberAlreadyActiveException(id);
        }

        member.activate();
        memberRepository.save(member);
    }

    public static class MemberAlreadyActiveException extends RuntimeException {

        public MemberAlreadyActiveException(Long id) {
            super("Member with id " + id + " is already active");
        }
    }

    public static class AlreadyInactiveException extends RuntimeException {

        public AlreadyInactiveException(Long id) {
            super("Member with id " + id + " is already inactive");
        }
    }

    @Transactional(readOnly = true)
    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Member findByUserId(Long userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new MemberNotFoundException(
                        "Member profile for user " + userId + " not found"
                ));
    }

    @Transactional(readOnly = true)
    public Member findByUsername(String username) {
        Long userId = identityFacade.findUserIdByUsername(username);
        return findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Page<Member> findAll(
            Boolean activeFilter,
            Pageable pageable) {

        if (activeFilter == null) {
            return memberRepository.findAll(pageable);
        }

        return memberRepository.findByActive(activeFilter, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<TrainerInfo> findTrainerOf(Long memberId) {
        return trainerMemberRepository.findByMemberId(memberId)
                .map(tm -> new TrainerInfo(
                        tm.getTrainer().getId(),
                        tm.getTrainer().getFullName()
                ));
    }

    @Transactional
    public Membership changePlan(Long memberId, Long planId) {

        // 1. Cancelar la membresía activa anterior
        membershipService.findActiveByMemberId(memberId)
                .ifPresent(m -> membershipService.cancel(m.getId()));

        // 2. Crear la nueva membresía
        Membership newMembership = membershipService.create(
                memberId,
                planId,
                LocalDate.now()
        );

        // 3. Registrar el pago inicial de la nueva membresía
        paymentService.record(
                newMembership.getId(),
                newMembership.getLockedPrice(),
                Payment.Method.CASH,
                LocalDate.now()
        );

        return newMembership;
    }

    @Transactional
    public void assignTrainer(Long memberId, Long trainerId) {
        if (!memberRepository.existsById(memberId)) {
            throw new MemberNotFoundException(memberId);
        }

        trainerMemberRepository.findByMemberId(memberId)
                .ifPresent(trainerMemberRepository::delete);

        trainerMemberRepository.flush();

        if (trainerId != null) {
            trainerMemberRepository.save(
                    new TrainerMember(
                            trainerService.findById(trainerId),
                            memberId
                    )
            );
        }
    }

    @Transactional
    public Member updateFullProfile(
            Long id,
            UpdateMemberRequest request) {

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));

        updateFullName(member, request.fullName());
        updateContact(member, request.contact());
        updateDocumentNumber(member, request.documentNumber());
        updateCredentials(member, request.username(), request.password());
        updatePlan(member, request.planId());
        updateTrainer(member, request.trainerId());

        return memberRepository.save(member);
    }

    private void updateFullName(Member member, String fullName) {
        if (fullName != null && !fullName.isBlank()) {
            member.setFullName(fullName);
        }
    }

    private void updateContact(Member member, String contact) {
        if (contact != null && !contact.isBlank()) {
            member.setContact(contact);
        }
    }

    private void updateDocumentNumber(Member member, String documentNumber) {
        if (documentNumber == null || documentNumber.isBlank()) {
            return;
        }

        boolean isDifferent = !Objects.equals(
                member.getDocumentNumber(), documentNumber);

        if (isDifferent && memberRepository.existsByDocumentNumber(documentNumber)) {
            throw new DuplicateDocumentNumberException(documentNumber);
        }

        member.setDocumentNumber(documentNumber);
    }

    private void updateCredentials(Member member, String username, String password) {
        boolean hasUsername = username != null && !username.isBlank();
        boolean hasPassword = password != null && !password.isBlank();

        if (hasUsername || hasPassword) {
            identityFacade.updateUser(
                    member.getUserId(),
                    username,
                    password,
                    null
            );
        }
    }

    private void updatePlan(Member member, Long planId) {
        if (planId != null) {
            changePlan(member.getId(), planId);
        }
    }

    private void updateTrainer(Member member, Long trainerId) {
        assignTrainer(member.getId(), trainerId);
    }
}
