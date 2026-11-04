package com.gms.members.api;

import com.gms.identity.controller.AuthTestHelper;
import com.gms.identity.entity.Role;
import com.gms.identity.entity.User;
import com.gms.identity.repository.UserRepository;
import com.gms.members.repository.MemberRepository;
import com.gms.plans.entity.Plan;
import com.gms.plans.repository.PlanRepository;
import com.gms.training.entity.Trainer;
import com.gms.training.repository.TrainerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class MemberControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private PlanRepository planRepository;
    @Autowired private TrainerRepository trainerRepository;
    @Autowired private AuthTestHelper auth;

    private Long planId;
    private Long trainerId;

    @BeforeEach
    void setUp() {
        planId = planRepository.save(new Plan("TestPlan-" + System.nanoTime(),
                new BigDecimal("50000.00"), 30)).getId();

        User trainerUser = userRepository.save(
                new User("trainer_mc_" + System.nanoTime(), "$2a$10$dummy", Role.TRAINER));
        trainerId = trainerRepository.save(new Trainer(trainerUser.getId(), "Trainer")).getId();
    }

    @AfterEach
    void cleanup() {
        trainerRepository.deleteAll();
    }

    @Test
    void registerMember_whenValidRequest_returns201() throws Exception {
        String body = """
                {
                  "username": "testmember1",
                  "password": "secret456",
                  "documentNumber": "TEST-001",
                  "fullName": "Test Member",
                  "contact": "123",
                  "planId": %d,
                  "initialPaymentMethod": "CASH",
                  "trainerId": %d
                }
                """.formatted(planId, trainerId);

        mockMvc.perform(post("/members")
                        .header("Authorization", "Bearer " + auth.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documentNumber").value("TEST-001"))
                .andExpect(jsonPath("$.active").value(true));

        assertThat(userRepository.existsByUsername("testmember1")).isTrue();
        assertThat(memberRepository.existsByDocumentNumber("TEST-001")).isTrue();
    }

    @Test
    void registerMember_whenDuplicateDocumentNumber_returns409() throws Exception {
        String first = """
                {"username":"first1","password":"secret456","documentNumber":"TEST-002",
                 "fullName":"First","contact":"123","planId":%d,"initialPaymentMethod":"CASH"}
                """.formatted(planId);
        mockMvc.perform(post("/members")
                        .header("Authorization", "Bearer " + auth.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(first))
                .andExpect(status().isCreated());

        String second = """
                {"username":"second1","password":"secret456","documentNumber":"TEST-002",
                 "fullName":"Second","contact":"456","planId":%d,"initialPaymentMethod":"CASH"}
                """.formatted(planId);
        mockMvc.perform(post("/members")
                        .header("Authorization", "Bearer " + auth.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(second))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate document number"));
    }

    @Test
    void registerMember_whenDuplicateUsername_returns409() throws Exception {
        String first = """
                {"username":"sameuser1","password":"secret456","documentNumber":"TEST-003",
                 "fullName":"First","contact":"123","planId":%d,"initialPaymentMethod":"CASH"}
                """.formatted(planId);
        mockMvc.perform(post("/members")
                        .header("Authorization", "Bearer " + auth.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(first))
                .andExpect(status().isCreated());

        String second = """
                {"username":"sameuser1","password":"secret456","documentNumber":"TEST-004",
                 "fullName":"Second","contact":"456","planId":%d,"initialPaymentMethod":"CASH"}
                """.formatted(planId);
        mockMvc.perform(post("/members")
                        .header("Authorization", "Bearer " + auth.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(second))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate username"));
    }

    @Test
    void registerMember_withoutToken_returns401() throws Exception {
        String body = """
                {"username":"noauth","password":"secret456","documentNumber":"TEST-999",
                 "fullName":"No Auth","contact":"123","planId":%d,"initialPaymentMethod":"CASH"}
                """.formatted(planId);
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerMember_withMemberRole_returns403() throws Exception {
        String body = """
                {"username":"memberrole","password":"secret456","documentNumber":"TEST-888",
                 "fullName":"Member Role","contact":"123","planId":%d,"initialPaymentMethod":"CASH"}
                """.formatted(planId);
        mockMvc.perform(post("/members")
                        .header("Authorization", "Bearer " + auth.memberToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }
}