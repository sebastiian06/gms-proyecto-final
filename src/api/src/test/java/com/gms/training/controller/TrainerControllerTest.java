package com.gms.training.controller;

import com.gms.identity.controller.AuthTestHelper;
import com.gms.identity.repository.UserRepository;
import com.gms.training.entity.Trainer;
import com.gms.training.repository.TrainerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class TrainerControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private TrainerRepository trainerRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AuthTestHelper auth;

    @AfterEach
    void cleanup() {
        trainerRepository.deleteAll();
    }

    @Test
    void createTrainer_asAdmin_returns201() throws Exception {
        String body = """
                {"username":"trainer1","password":"secret123","fullName":"Carlos Trainer"}
                """;
        mockMvc.perform(post("/trainers")
                        .header("Authorization", "Bearer " + auth.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Carlos Trainer"))
                .andExpect(jsonPath("$.userId").isNumber());

        assertThat(trainerRepository.findAll()).hasSize(1);
    }

    @Test
    void createTrainer_asReceptionist_returns403() throws Exception {
        String body = """
                {"username":"trainer2","password":"secret123","fullName":"X"}
                """;
        mockMvc.perform(post("/trainers")
                        .header("Authorization", "Bearer " + auth.receptionistToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void listTrainers_asReceptionist_returns200() throws Exception {
        mockMvc.perform(get("/trainers")
                        .header("Authorization", "Bearer " + auth.receptionistToken()))
                .andExpect(status().isOk());
    }

    @Test
    void listTrainers_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/trainers"))
                .andExpect(status().isUnauthorized());
    }
}