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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class ListMembershipsControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private MembershipRepository membershipRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private PlanRepository planRepository;
    @Autowired private AuthTestHelper auth;

    private Member member1;
    private Member member2;
    private Plan plan;

    private void setupData() {
        member1 = memberRepository.save(new Member(900L, "DOC-LIST-1", "Member 1", "123"));
        member2 = memberRepository.save(new Member(901L, "DOC-LIST-2", "Member 2", "456"));
        plan = planRepository.save(new Plan("Basic", new BigDecimal("50000.00"), 30));
    }

    @Test
    void listMemberships_whenAuthenticated_returnsPage() throws Exception {
        setupData();
        membershipRepository.save(new Membership(member1.getId(), plan.getId(),
                new BigDecimal("50000.00"), LocalDate.now(), LocalDate.now().plusDays(30)));
        membershipRepository.save(new Membership(member2.getId(), plan.getId(),
                new BigDecimal("50000.00"), LocalDate.now(), LocalDate.now().plusDays(30)));

        mockMvc.perform(get("/memberships")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page.totalElements").value(2));
    }

    @Test
    void listMemberships_whenFilteredByMemberId_returnsOnlyThatMember() throws Exception {
        setupData();
        membershipRepository.save(new Membership(member1.getId(), plan.getId(),
                new BigDecimal("50000.00"), LocalDate.now(), LocalDate.now().plusDays(30)));
        membershipRepository.save(new Membership(member2.getId(), plan.getId(),
                new BigDecimal("50000.00"), LocalDate.now(), LocalDate.now().plusDays(30)));

        mockMvc.perform(get("/memberships?memberId=" + member1.getId())
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].memberId").value(member1.getId()));
    }

    @Test
    void getMembership_whenExists_returnsIt() throws Exception {
        setupData();
        Membership m = membershipRepository.save(new Membership(member1.getId(), plan.getId(),
                new BigDecimal("50000.00"), LocalDate.now(), LocalDate.now().plusDays(30)));

        mockMvc.perform(get("/memberships/" + m.getId())
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(m.getId()))
                .andExpect(jsonPath("$.memberId").value(member1.getId()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getMembership_whenNotFound_returns404() throws Exception {
        mockMvc.perform(get("/memberships/999999")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Membership not found"));
    }

    @Test
    void listMemberships_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/memberships"))
                .andExpect(status().isUnauthorized());
    }
}