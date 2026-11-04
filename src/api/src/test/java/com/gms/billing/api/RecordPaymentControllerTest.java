package com.gms.billing.api;

import com.gms.billing.entity.Membership;
import com.gms.billing.entity.Payment;
import com.gms.billing.repository.MembershipRepository;
import com.gms.billing.repository.PaymentRepository;
import com.gms.identity.controller.AuthTestHelper;
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
class RecordPaymentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private MembershipRepository membershipRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private AuthTestHelper auth;

    private Membership createMembership(BigDecimal lockedPrice) {
        return membershipRepository.save(new Membership(
                999L, 1L, lockedPrice,
                LocalDate.now(), LocalDate.now().plusDays(30)));
    }

    @Test
    void recordPayment_whenValid_returns201() throws Exception {
        Membership m = createMembership(new BigDecimal("50000.00"));

        String body = """
                {"amount":50000.00,"method":"CASH","recordedDate":"2026-11-01"}
                """;

        mockMvc.perform(post("/memberships/" + m.getId() + "/payments")
                        .header("Authorization", "Bearer " + auth.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.membershipId").value(m.getId()))
                .andExpect(jsonPath("$.amount").value(50000.00))
                .andExpect(jsonPath("$.method").value("CASH"));

        assertThat(paymentRepository.findByMembershipId(m.getId())).hasSize(1);
    }

    @Test
    void recordPayment_whenPartialThenExceeds_returns409() throws Exception {
        Membership m = createMembership(new BigDecimal("50000.00"));

        String first = """
                {"amount":30000.00,"method":"CASH","recordedDate":"2026-11-01"}
                """;
        mockMvc.perform(post("/memberships/" + m.getId() + "/payments")
                        .header("Authorization", "Bearer " + auth.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(first))
                .andExpect(status().isCreated());

        String second = """
                {"amount":30000.00,"method":"CASH","recordedDate":"2026-11-01"}
                """;
        mockMvc.perform(post("/memberships/" + m.getId() + "/payments")
                        .header("Authorization", "Bearer " + auth.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(second))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Payment exceeds membership balance"));
    }

    @Test
    void recordPayment_whenMembershipNotFound_returns404() throws Exception {
        String body = """
                {"amount":1000.00,"method":"CASH","recordedDate":"2026-11-01"}
                """;
        mockMvc.perform(post("/memberships/999999/payments")
                        .header("Authorization", "Bearer " + auth.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Membership not found"));
    }

    @Test
    void recordPayment_withoutToken_returns401() throws Exception {
        String body = """
                {"amount":1000.00,"method":"CASH","recordedDate":"2026-11-01"}
                """;
        mockMvc.perform(post("/memberships/1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}