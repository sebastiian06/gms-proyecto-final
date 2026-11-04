package com.gms.training.controller;

import com.gms.identity.controller.AuthTestHelper;
import com.gms.identity.entity.Role;
import com.gms.identity.entity.User;
import com.gms.identity.repository.UserRepository;
import com.gms.training.entity.Trainer;
import com.gms.training.repository.RoutineRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class RoutineControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private RoutineRepository routineRepository;
    @Autowired private TrainerRepository trainerRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AuthTestHelper auth;

    private Long trainerId;

    @BeforeEach
    void setUp() {
        User u = userRepository.save(new User("trainer_rt", "$2a$10$dummy", Role.TRAINER));
        Trainer t = trainerRepository.save(new Trainer(u.getId(), "Test Trainer"));
        trainerId = t.getId();
    }

    @AfterEach
    void cleanup() {
        routineRepository.deleteAll();
        trainerRepository.deleteAll();
    }

    @Test
    void createRoutine_asTrainer_returns201() throws Exception {
        String body = """
                {"name":"Full Body"}
                """;
        mockMvc.perform(post("/routines/trainers/" + trainerId)
                        .header("Authorization", "Bearer " + auth.trainerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Full Body"))
                .andExpect(jsonPath("$.trainerId").value(trainerId));
    }

    @Test
    void createRoutine_asAdmin_returns403() throws Exception {
        String body = """
                {"name":"X"}
                """;
        mockMvc.perform(post("/routines/trainers/" + trainerId)
                        .header("Authorization", "Bearer " + auth.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void listRoutinesByTrainer_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/routines/trainers/" + trainerId)
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isOk());
    }

    @Test
    void getRoutine_whenNotFound_returns404() throws Exception {
        mockMvc.perform(get("/routines/999999")
                        .header("Authorization", "Bearer " + auth.trainerToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMemberRoutine_whenNoAssignment_returns404() throws Exception {
        mockMvc.perform(get("/routines/members/999999")
                        .header("Authorization", "Bearer " + auth.trainerToken()))
                .andExpect(status().isNotFound());
    }
}