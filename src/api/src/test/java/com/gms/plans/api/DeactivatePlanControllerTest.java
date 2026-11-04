package com.gms.plans.api;

import com.gms.identity.controller.AuthTestHelper;
import com.gms.plans.entity.Plan;
import com.gms.plans.repository.PlanRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class DeactivatePlanControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PlanRepository planRepository;
    @Autowired private AuthTestHelper auth;

    @Test
    void deactivatePlan_whenActive_returns204() throws Exception {
        Plan plan = planRepository.save(new Plan("Basic", new BigDecimal("50000.00"), 30));

        mockMvc.perform(delete("/plans/" + plan.getId())
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isNoContent());

        Plan updated = planRepository.findById(plan.getId()).orElseThrow();
        assertThat(updated.getActive()).isFalse();
    }

    @Test
    void deactivatePlan_whenAlreadyInactive_returns409() throws Exception {
        Plan plan = new Plan("Basic", new BigDecimal("50000.00"), 30);
        plan.deactivate();
        Long id = planRepository.save(plan).getId();

        mockMvc.perform(delete("/plans/" + id)
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Plan already inactive"));
    }

    @Test
    void deactivatePlan_whenNotFound_returns404() throws Exception {
        mockMvc.perform(delete("/plans/999999")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Plan not found"));
    }

    @Test
    void deactivatePlan_withoutToken_returns401() throws Exception {
        Plan plan = planRepository.save(new Plan("Basic", new BigDecimal("50000.00"), 30));

        mockMvc.perform(delete("/plans/" + plan.getId()))
                .andExpect(status().isUnauthorized());
    }
}