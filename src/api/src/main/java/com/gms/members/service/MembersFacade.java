
package com.gms.members.service;

import com.gms.billing.dto.MembershipDto;
import com.gms.billing.entity.Membership;
import com.gms.identity.service.IdentityFacade;
import com.gms.members.dto.CreateMemberRequest;
import com.gms.members.dto.MemberDto;
import com.gms.members.dto.UpdateMemberRequest;
import com.gms.members.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class MembersFacade {

    private final MemberService memberService;
    private final IdentityFacade identityFacade;

    public MembersFacade(
            MemberService memberService,
            IdentityFacade identityFacade) {
        this.memberService = memberService;
        this.identityFacade = identityFacade;
    }

    public MemberDto registerMember(CreateMemberRequest request) {
        return toDto(memberService.registerMember(request));
    }

    public MemberDto updateProfile(
            Long id,
            String fullName,
            String contact) {

        return toDto(
                memberService.updateProfile(id, fullName, contact)
        );
    }

    public MemberDto updateFullProfile(
            Long id,
            UpdateMemberRequest request) {

        return toDto(
                memberService.updateFullProfile(id, request)
        );
    }

    public void deactivate(Long id) {
        memberService.deactivate(id);
    }

    public void activate(Long id) {
        memberService.activate(id);
    }

    public MemberDto findById(Long id) {
        return toDto(memberService.findById(id));
    }

    public MemberDto findByUserId(Long userId) {
        return toDto(memberService.findByUserId(userId));
    }

    public MemberDto findByUsername(String username) {
        return toDto(memberService.findByUsername(username));
    }

    public Page<MemberDto> findAll(
            Boolean activeFilter,
            Pageable pageable) {

        return memberService.findAll(activeFilter, pageable)
                .map(this::toDto);
    }

    public MembershipDto changePlan(Long memberId, Long planId) {
        Membership membership = memberService.changePlan(memberId, planId);
        return MembershipDto.from(membership);
    }

    public void assignTrainer(Long memberId, Long trainerId) {
        memberService.assignTrainer(memberId, trainerId);
    }

    public String regenerateAccessCode(Long memberId) {
        Member member = memberService.findById(memberId);

        return identityFacade.regenerateAccessCode(member.getUserId());
    }

    private MemberDto toDto(Member member) {

        String accessCode = identityFacade
                .findActiveAccessCode(member.getUserId())
                .orElse(null);

        MemberService.TrainerInfo trainer = memberService
                .findTrainerOf(member.getId())
                .orElse(null);

        return MemberDto.from(
                member,
                trainer != null ? trainer.id() : null,
                trainer != null ? trainer.name() : null,
                accessCode
        );
    }
}
