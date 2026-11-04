package com.gms.plans.api;

import com.gms.identity.controller.AuthTestHelper;
import com.gms.plans.repository.PlanRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class CreatePlanControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PlanRepository planRepository;
    @Autowired private AuthTestHelper auth;

    @Test
    void createPlan_whenValidRequest_returns201() throws Exception {
        String body = """
                {"name":"Basic","price":50000.00,"durationDays":30}
                """;

        mockMvc.perform(post("/plans")
                        .header("Authorization", "Bearer " + auth.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Basic"))
                .andExpect(jsonPath("$.price").value(50000.00))
                .andExpect(jsonPath("$.durationDays").value(30))
                .andExpect(jsonPath("$.active").value(true));

        assertThat(planRepository.existsByNameAndActiveTrue("Basic")).isTrue();
    }

    @Test
    void createPlan_whenDuplicateName_returns409() throws Exception {
        String body = """
                {"name":"Basic","price":50000.00,"durationDays":30}
                """;

        mockMvc.perform(post("/plans")
                        .header("Authorization", "Bearer " + auth.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/plans")
                        .header("Authorization", "Bearer " + auth.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate plan name"));
    }

    @Test
    void createPlan_withoutToken_returns401() throws Exception {
        String body = """
                {"name":"Basic","price":50000.00,"durationDays":30}
                """;

        mockMvc.perform(post("/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}