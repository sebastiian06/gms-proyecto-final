package com.gms.billing.api;

import com.gms.billing.entity.Membership;
import com.gms.billing.repository.MembershipRepository;
import com.gms.identity.controller.AuthTestHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class DeactivateMembershipControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private MembershipRepository membershipRepository;
    @Autowired private AuthTestHelper auth;

    @Test
    void cancelMembership_whenActive_returns204() throws Exception {
        Membership m = membershipRepository.save(new Membership(
                999L, 1L, new BigDecimal("50000.00"),
                LocalDate.now(), LocalDate.now().plusDays(30)));

        mockMvc.perform(delete("/memberships/" + m.getId())
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isNoContent());

        Membership updated = membershipRepository.findById(m.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(Membership.Status.CANCELLED);
    }

    @Test
    void cancelMembership_whenAlreadyCancelled_returns409() throws Exception {
        Membership m = new Membership(
                998L, 1L, new BigDecimal("50000.00"),
                LocalDate.now(), LocalDate.now().plusDays(30));
        m.cancel();
        Long id = membershipRepository.save(m).getId();

        mockMvc.perform(delete("/memberships/" + id)
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Membership not active"));
    }

    @Test
    void cancelMembership_whenNotFound_returns404() throws Exception {
        mockMvc.perform(delete("/memberships/999999")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Membership not found"));
    }

    @Test
    void cancelMembership_withoutToken_returns401() throws Exception {
        Membership m = membershipRepository.save(new Membership(
                997L, 1L, new BigDecimal("50000.00"),
                LocalDate.now(), LocalDate.now().plusDays(30)));

        mockMvc.perform(delete("/memberships/" + m.getId()))
                .andExpect(status().isUnauthorized());
    }
}