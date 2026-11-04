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
class ListPaymentsControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private MembershipRepository membershipRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private AuthTestHelper auth;

    private Membership createMembership() {
        return membershipRepository.save(new Membership(
                999L, 1L, new BigDecimal("50000.00"),
                LocalDate.now(), LocalDate.now().plusDays(30)));
    }

    @Test
    void listPayments_whenAuthenticated_returnsPayments() throws Exception {
        Membership m = createMembership();
        paymentRepository.save(new Payment(m, new BigDecimal("30000.00"),
                Payment.Method.CASH, LocalDate.now()));
        paymentRepository.save(new Payment(m, new BigDecimal("20000.00"),
                Payment.Method.TRANSFER, LocalDate.now()));

        mockMvc.perform(get("/memberships/" + m.getId() + "/payments")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].membershipId").value(m.getId()))
                .andExpect(jsonPath("$[0].method").value("CASH"))
                .andExpect(jsonPath("$[1].method").value("TRANSFER"));
    }

    @Test
    void listPayments_whenNoPayments_returnsEmptyArray() throws Exception {
        Membership m = createMembership();

        mockMvc.perform(get("/memberships/" + m.getId() + "/payments")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void listPayments_whenMembershipNotFound_returns404() throws Exception {
        mockMvc.perform(get("/memberships/999999/payments")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Membership not found"));
    }

    @Test
    void listPayments_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/memberships/1/payments"))
                .andExpect(status().isUnauthorized());
    }
}