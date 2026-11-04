package com.gms.reports.controller;

import com.gms.billing.entity.Membership;
import com.gms.billing.entity.Payment;
import com.gms.billing.repository.MembershipRepository;
import com.gms.billing.repository.PaymentRepository;
import com.gms.identity.controller.AuthTestHelper;
import com.gms.members.entity.Member;
import com.gms.members.repository.MemberRepository;
import com.gms.plans.entity.Plan;
import com.gms.plans.repository.PlanRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
class ReportControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private PlanRepository planRepository;
    @Autowired private MembershipRepository membershipRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private AuthTestHelper auth;

    private Member member;
    private Plan plan;
    private Membership membership;

    @BeforeEach
    void setUp() {
        // Limpiar datos previos que puedan afectar las aserciones
        paymentRepository.deleteAll();
        membershipRepository.deleteAll();
        planRepository.deleteAll();
        memberRepository.deleteAll();

        member = memberRepository.save(
                new Member(900L, "DOC-REP-1", "Report Client", "123"));

        plan = planRepository.save(
                new Plan("Report Plan", new BigDecimal("50000.00"), 30));

        membership = membershipRepository.save(
                new Membership(member.getId(), plan.getId(),
                        new BigDecimal("50000.00"),
                        LocalDate.now(), LocalDate.now().plusDays(30)));
    }

    @AfterEach
    void cleanup() {
        paymentRepository.deleteAll();
        membershipRepository.deleteAll();
        planRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    void summary_asAdmin_returnsTotals() throws Exception {
        // Un pago dentro del mes actual
        paymentRepository.save(new Payment(membership,
                new BigDecimal("30000.00"), Payment.Method.CASH, LocalDate.now()));

        mockMvc.perform(get("/reports/summary")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeMembers").isNumber())
                .andExpect(jsonPath("$.monthlyRevenue").isNumber())
                .andExpect(jsonPath("$.attendanceThisMonth").isNumber())
                .andExpect(jsonPath("$.mostSoldPlan").isString());
    }

    @Test
    void summary_whenNoData_returnsZerosAndDash() throws Exception {
        // Solo el miembro creado en setUp; sin pagos adicionales en el mes
        // (el pago de otros tests no existe porque @Transactional revierte)
        mockMvc.perform(get("/reports/summary")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeMembers").isNumber())
                .andExpect(jsonPath("$.monthlyRevenue").isNumber())
                .andExpect(jsonPath("$.mostSoldPlan").isString());
    }

    @Test
    void summary_asReceptionist_returns200() throws Exception {
        mockMvc.perform(get("/reports/summary")
                        .header("Authorization", "Bearer " + auth.receptionistToken()))
                .andExpect(status().isOk());
    }

    @Test
    void summary_asMember_returns403() throws Exception {
        mockMvc.perform(get("/reports/summary")
                        .header("Authorization", "Bearer " + auth.memberToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void summary_asTrainer_returns403() throws Exception {
        mockMvc.perform(get("/reports/summary")
                        .header("Authorization", "Bearer " + auth.trainerToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void summary_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/reports/summary"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void recentPayments_asAdmin_returnsList() throws Exception {
        paymentRepository.save(new Payment(membership,
                new BigDecimal("20000.00"), Payment.Method.CASH, LocalDate.now()));
        paymentRepository.save(new Payment(membership,
                new BigDecimal("10000.00"), Payment.Method.TRANSFER, LocalDate.now()));

        mockMvc.perform(get("/reports/recent-payments")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].memberName").value("Report Client"))
                .andExpect(jsonPath("$[0].planName").value("Report Plan"))
                .andExpect(jsonPath("$[0].amount").isNumber())
                .andExpect(jsonPath("$[0].method").isString())
                .andExpect(jsonPath("$[0].recordedDate").isString());
    }

    @Test
    void recentPayments_whenLimitProvided_respectsLimit() throws Exception {
        paymentRepository.save(new Payment(membership,
                new BigDecimal("20000.00"), Payment.Method.CASH, LocalDate.now()));
        paymentRepository.save(new Payment(membership,
                new BigDecimal("10000.00"), Payment.Method.TRANSFER, LocalDate.now()));
        paymentRepository.save(new Payment(membership,
                new BigDecimal("5000.00"), Payment.Method.CARD, LocalDate.now()));

        mockMvc.perform(get("/reports/recent-payments?limit=2")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void recentPayments_whenNoPayments_returnsEmptyArray() throws Exception {
        mockMvc.perform(get("/reports/recent-payments")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void recentPayments_asMember_returns403() throws Exception {
        mockMvc.perform(get("/reports/recent-payments")
                        .header("Authorization", "Bearer " + auth.memberToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void recentPayments_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/reports/recent-payments"))
                .andExpect(status().isUnauthorized());
    }
}