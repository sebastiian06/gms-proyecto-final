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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class UpdatePlanControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PlanRepository planRepository;
    @Autowired private AuthTestHelper auth;

    @Test
    void updatePlan_whenValidRequest_returns200() throws Exception {
        Plan plan = planRepository.save(new Plan("Basic", new BigDecimal("50000.00"), 30));

        String body = """
                {"name":"Basic Updated","price":55000.00,"durationDays":45}
                """;

        mockMvc.perform(put("/plans/" + plan.getId())
                        .header("Authorization", "Bearer " + auth.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Basic Updated"))
                .andExpect(jsonPath("$.price").value(55000.00))
                .andExpect(jsonPath("$.durationDays").value(45));

        Plan updated = planRepository.findById(plan.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Basic Updated");
        assertThat(updated.getPrice()).isEqualByComparingTo(new BigDecimal("55000.00"));
        assertThat(updated.getDurationDays()).isEqualTo(45);
    }

    @Test
    void updatePlan_whenNameCollidesWithAnotherActive_returns409() throws Exception {
        planRepository.save(new Plan("Basic", new BigDecimal("50000.00"), 30));
        Plan premium = planRepository.save(new Plan("Premium", new BigDecimal("80000.00"), 30));

        String body = """
                {"name":"Basic","price":90000.00,"durationDays":30}
                """;

        mockMvc.perform(put("/plans/" + premium.getId())
                        .header("Authorization", "Bearer " + auth.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate plan name"));
    }

    @Test
    void updatePlan_whenNotFound_returns404() throws Exception {
        String body = """
                {"name":"Does Not Matter","price":1000.00,"durationDays":10}
                """;

        mockMvc.perform(put("/plans/999999")
                        .header("Authorization", "Bearer " + auth.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Plan not found"));
    }

    @Test
    void updatePlan_withoutToken_returns401() throws Exception {
        Plan plan = planRepository.save(new Plan("Basic", new BigDecimal("50000.00"), 30));

        String body = """
                {"name":"Basic Updated","price":55000.00,"durationDays":45}
                """;

        mockMvc.perform(put("/plans/" + plan.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}