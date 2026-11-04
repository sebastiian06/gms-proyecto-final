package com.gms.plans.api;

import com.gms.identity.controller.AuthTestHelper;
import com.gms.plans.entity.Plan;
import com.gms.plans.repository.PlanRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class ActivatePlanControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PlanRepository planRepository;
    @Autowired private AuthTestHelper auth;

    @Test
    void activatePlan_whenInactive_returns204() throws Exception {
        Plan plan = new Plan("Basic", new BigDecimal("50000.00"), 30);
        plan.deactivate();
        Long id = planRepository.save(plan).getId();

        mockMvc.perform(patch("/plans/" + id + "/activate")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isNoContent());

        assertThat(planRepository.findById(id).orElseThrow().getActive()).isTrue();
    }

    @Test
    void activatePlan_whenAlreadyActive_returns409() throws Exception {
        Long id = planRepository.save(new Plan("Basic", new BigDecimal("50000.00"), 30)).getId();

        mockMvc.perform(patch("/plans/" + id + "/activate")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Plan already active"));
    }

    @Test
    void activatePlan_whenNotFound_returns404() throws Exception {
        mockMvc.perform(patch("/plans/999999/activate")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isNotFound());
    }
}