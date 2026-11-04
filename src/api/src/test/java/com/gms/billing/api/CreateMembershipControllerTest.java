package com.gms.billing.api;

import com.gms.billing.entity.Membership;
import com.gms.billing.repository.MembershipRepository;
import com.gms.identity.controller.AuthTestHelper;
import com.gms.members.entity.Member;
import com.gms.members.repository.MemberRepository;
import com.gms.plans.entity.Plan;
import com.gms.plans.repository.PlanRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class CreateMembershipControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private MembershipRepository membershipRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private PlanRepository planRepository;
    @Autowired private AuthTestHelper auth;

    @Test
    void createMembership_whenValid_returns201() throws Exception {
        Member member = memberRepository.save(new Member(999L, "DOC-MEM-1", "Test Member", "123"));
        Plan plan = planRepository.save(new Plan("Basic", new BigDecimal("50000.00"), 30));

        String body = String.format("""
                {"memberId":%d,"planId":%d,"startDate":"2026-11-01"}
                """, member.getId(), plan.getId());

        mockMvc.perform(post("/memberships")
                        .header("Authorization", "Bearer " + auth.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").value(member.getId()))
                .andExpect(jsonPath("$.planId").value(plan.getId()))
                .andExpect(jsonPath("$.lockedPrice").value(50000.00))
                .andExpect(jsonPath("$.startDate").value("2026-11-01"))
                .andExpect(jsonPath("$.endDate").value("2026-12-01"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        assertThat(membershipRepository.existsByMemberIdAndStatus(
                member.getId(), Membership.Status.ACTIVE)).isTrue();
    }

    @Test
    void createMembership_whenMemberHasActiveMembership_returns409() throws Exception {
        Member member = memberRepository.save(new Member(998L, "DOC-MEM-2", "Test", "123"));
        Plan plan = planRepository.save(new Plan("Basic", new BigDecimal("50000.00"), 30));

        String body = String.format("""
                {"memberId":%d,"planId":%d,"startDate":"2026-11-01"}
                """, member.getId(), plan.getId());

        mockMvc.perform(post("/memberships")
                        .header("Authorization", "Bearer " + auth.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/memberships")
                        .header("Authorization", "Bearer " + auth.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Active membership already exists"));
    }

    @Test
    void createMembership_whenPlanInactive_returns409() throws Exception {
        Member member = memberRepository.save(new Member(997L, "DOC-MEM-3", "Test", "123"));
        Plan plan = new Plan("Retired", new BigDecimal("30000.00"), 30);
        plan.deactivate();
        Long planId = planRepository.save(plan).getId();

        String body = String.format("""
                {"memberId":%d,"planId":%d,"startDate":"2026-11-01"}
                """, member.getId(), planId);

        mockMvc.perform(post("/memberships")
                        .header("Authorization", "Bearer " + auth.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Inactive"));
    }

    @Test
    void createMembership_withoutToken_returns401() throws Exception {
        String body = """
                {"memberId":1,"planId":1,"startDate":"2026-11-01"}
                """;
        mockMvc.perform(post("/memberships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}