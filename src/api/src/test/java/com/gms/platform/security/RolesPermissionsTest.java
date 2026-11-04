package com.gms.platform.security;

import com.gms.billing.repository.MembershipRepository;
import com.gms.billing.repository.PaymentRepository;
import com.gms.identity.controller.AuthTestHelper;
import com.gms.identity.entity.Role;
import com.gms.identity.entity.User;
import com.gms.identity.repository.UserRepository;
import com.gms.members.repository.MemberRepository;
import com.gms.plans.entity.Plan;
import com.gms.plans.repository.PlanRepository;
import com.gms.training.entity.Trainer;
import com.gms.training.repository.RoutineRepository;
import com.gms.training.repository.TrainerMemberRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class RolesPermissionsTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AuthTestHelper auth;
    @Autowired private MemberRepository memberRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PlanRepository planRepository;
    @Autowired private MembershipRepository membershipRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private TrainerRepository trainerRepository;
    @Autowired private TrainerMemberRepository trainerMemberRepository;
    @Autowired private RoutineRepository routineRepository;

    private Long planId;
    private Long trainerId;

    @BeforeEach
    void setUp() {
        planId = planRepository.save(new Plan("TestPlan-" + System.nanoTime(),
                new BigDecimal("50000.00"), 30)).getId();

        User trainerUser = userRepository.save(
                new User("trainer_rp_" + System.nanoTime(), "$2a$10$dummy", Role.TRAINER));
        trainerId = trainerRepository.save(new Trainer(trainerUser.getId(), "Trainer")).getId();
    }

    @AfterEach
    void cleanup() {
        routineRepository.deleteAll();
        trainerMemberRepository.deleteAll();
        trainerRepository.deleteAll();
        paymentRepository.deleteAll();
        membershipRepository.deleteAll();
        planRepository.deleteAll();
        memberRepository.deleteAll();
        userRepository.deleteAll();
    }

    // --- Plans: only ADMIN can write ---

    @Test
    void createPlan_asReceptionist_returns403() throws Exception {
        String body = """
                {"name":"Gold","price":100000.00,"durationDays":30}
                """;
        mockMvc.perform(post("/plans")
                        .header("Authorization", "Bearer " + auth.receptionistToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void createPlan_asTrainer_returns403() throws Exception {
        String body = """
                {"name":"Gold2","price":100000.00,"durationDays":30}
                """;
        mockMvc.perform(post("/plans")
                        .header("Authorization", "Bearer " + auth.trainerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    // --- Plans: everyone authenticated can read ---

    @Test
    void listPlans_asTrainer_returns200() throws Exception {
        mockMvc.perform(get("/plans")
                        .header("Authorization", "Bearer " + auth.trainerToken()))
                .andExpect(status().isOk());
    }

    @Test
    void listPlans_asMember_returns200() throws Exception {
        mockMvc.perform(get("/plans")
                        .header("Authorization", "Bearer " + auth.memberToken()))
                .andExpect(status().isOk());
    }

    // --- Members: receptionist can write, trainer can only read ---

    @Test
    void createMember_asTrainer_returns403() throws Exception {
        String body = """
                {"username":"mem_t","password":"secret123","documentNumber":"DOC_T",
                 "fullName":"T","contact":"1","planId":%d,"initialPaymentMethod":"CASH",
                 "trainerId":%d}
                """.formatted(planId, trainerId);
        mockMvc.perform(post("/members")
                        .header("Authorization", "Bearer " + auth.trainerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void createMember_asReceptionist_returns201() throws Exception {
        String body = """
                {"username":"mem_r","password":"secret123","documentNumber":"DOC_R",
                 "fullName":"R","contact":"1","planId":%d,"initialPaymentMethod":"CASH",
                 "trainerId":%d}
                """.formatted(planId, trainerId);
        mockMvc.perform(post("/members")
                        .header("Authorization", "Bearer " + auth.receptionistToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    // --- Users: only ADMIN ---

    @Test
    void createUser_asReceptionist_returns403() throws Exception {
        String body = """
                {"username":"u1","password":"secret123","role":"ADMIN"}
                """;
        mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer " + auth.receptionistToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    // --- Trainers: only ADMIN can create ---

    @Test
    void createTrainer_asReceptionist_returns403() throws Exception {
        String body = """
                {"username":"t_test","password":"secret123","fullName":"X"}
                """;
        mockMvc.perform(post("/trainers")
                        .header("Authorization", "Bearer " + auth.receptionistToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    // --- Routines: MEMBER can read ---

    @Test
    void listRoutinesAsMember_returns403() throws Exception {
        mockMvc.perform(get("/routines/trainers/1")
                        .header("Authorization", "Bearer " + auth.memberToken()))
                .andExpect(status().isForbidden());
    }
}