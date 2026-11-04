package com.gms.training.controller;

import com.gms.identity.controller.AuthTestHelper;
import com.gms.identity.entity.Role;
import com.gms.identity.entity.User;
import com.gms.identity.repository.UserRepository;
import com.gms.training.entity.Routine;
import com.gms.training.entity.Trainer;
import com.gms.training.repository.ExerciseRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class ExerciseControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ExerciseRepository exerciseRepository;
    @Autowired private RoutineRepository routineRepository;
    @Autowired private TrainerRepository trainerRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AuthTestHelper auth;

    private Long routineId;

    @BeforeEach
    void setUp() {
        User u = userRepository.save(new User("trainer_ex", "$2a$10$dummy", Role.TRAINER));
        Trainer t = trainerRepository.save(new Trainer(u.getId(), "Test Trainer"));
        Routine r = routineRepository.save(new Routine(t, "Routine"));
        routineId = r.getId();
    }

    @AfterEach
    void cleanup() {
        exerciseRepository.deleteAll();
        routineRepository.deleteAll();
        trainerRepository.deleteAll();
    }

    @Test
    void createExercise_asTrainer_returns201() throws Exception {
        String body = """
                {"name":"Press banca","sets":4,"repetitions":10}
                """;
        mockMvc.perform(post("/routines/" + routineId + "/exercises")
                        .header("Authorization", "Bearer " + auth.trainerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Press banca"))
                .andExpect(jsonPath("$.sets").value(4))
                .andExpect(jsonPath("$.repetitions").value(10));
    }

    @Test
    void createExercise_asAdmin_returns403() throws Exception {
        String body = """
                {"name":"X","sets":1,"repetitions":1}
                """;
        mockMvc.perform(post("/routines/" + routineId + "/exercises")
                        .header("Authorization", "Bearer " + auth.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void createExercise_whenRoutineNotFound_returns404() throws Exception {
        String body = """
                {"name":"X","sets":1,"repetitions":1}
                """;
        mockMvc.perform(post("/routines/999999/exercises")
                        .header("Authorization", "Bearer " + auth.trainerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void createExercise_withInvalidSets_returns400() throws Exception {
        String body = """
                {"name":"X","sets":0,"repetitions":1}
                """;
        mockMvc.perform(post("/routines/" + routineId + "/exercises")
                        .header("Authorization", "Bearer " + auth.trainerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}