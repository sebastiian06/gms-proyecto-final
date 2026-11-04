
package com.gms.members.controller;

import com.gms.billing.dto.MembershipDto;
import com.gms.identity.service.JwtAuthFilter;
import com.gms.members.dto.ChangePlanRequest;
import com.gms.members.dto.CreateMemberRequest;
import com.gms.members.dto.MemberDto;
import com.gms.members.dto.UpdateMemberRequest;
import com.gms.members.service.MembersFacade;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MembersFacade membersFacade;

    public MemberController(MembersFacade membersFacade) {
        this.membersFacade = membersFacade;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @PostMapping
    public ResponseEntity<MemberDto> registerMember(
            @Valid @RequestBody CreateMemberRequest request) {

        MemberDto member = membersFacade.registerMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me")
    public ResponseEntity<MemberDto> getMyProfile(
            Authentication authentication) {

        JwtAuthFilter.AuthenticatedUser user =
                (JwtAuthFilter.AuthenticatedUser) authentication.getPrincipal();

        return ResponseEntity.ok(
                membersFacade.findByUsername(user.username())
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @PatchMapping("/{id}")
    public ResponseEntity<MemberDto> updateMember(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMemberRequest request) {

        MemberDto member = membersFacade.updateFullProfile(id, request);
        return ResponseEntity.ok(member);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @PatchMapping("/{id}/plan")
    public ResponseEntity<MembershipDto> changePlan(
            @PathVariable Long id,
            @Valid @RequestBody ChangePlanRequest request) {

        return ResponseEntity.ok(
                membersFacade.changePlan(id, request.planId())
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @PatchMapping("/{id}/trainer")
    public ResponseEntity<Void> assignTrainer(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {

        membersFacade.assignTrainer(id, body.get("trainerId"));
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @PostMapping("/{id}/regenerate-access-code")
    public ResponseEntity<Map<String, String>> regenerateAccessCode(
            @PathVariable Long id) {

        String code = membersFacade.regenerateAccessCode(id);
        return ResponseEntity.ok(Map.of("accessCode", code));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateMember(@PathVariable Long id) {
        membersFacade.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateMember(@PathVariable Long id) {
        membersFacade.activate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<MemberDto>> listMembers(
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(
                page,
                Math.min(size, 100),
                Sort.by("fullName").ascending()
        );

        return ResponseEntity.ok(
                membersFacade.findAll(active, pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberDto> getMember(@PathVariable Long id) {
        return ResponseEntity.ok(membersFacade.findById(id));
    }
}
