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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class ListPlansControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PlanRepository planRepository;
    @Autowired private AuthTestHelper auth;

    @Test
    void listPlans_whenAuthenticated_returnsPage() throws Exception {
        planRepository.save(new Plan("Basic", new BigDecimal("50000.00"), 30));
        planRepository.save(new Plan("Premium", new BigDecimal("80000.00"), 30));

        mockMvc.perform(get("/plans")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Basic"))
                .andExpect(jsonPath("$.content[1].name").value("Premium"))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.totalPages").value(1));
    }

    @Test
    void listPlans_whenActiveFalseFilter_returnsOnlyInactive() throws Exception {
        planRepository.save(new Plan("Basic", new BigDecimal("50000.00"), 30));
        Plan inactive = new Plan("Retired", new BigDecimal("30000.00"), 30);
        inactive.deactivate();
        planRepository.save(inactive);

        mockMvc.perform(get("/plans?active=false")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Retired"))
                .andExpect(jsonPath("$.content[0].active").value(false));
    }

    @Test
    void getPlan_whenExists_returnsPlan() throws Exception {
        Plan plan = planRepository.save(new Plan("Basic", new BigDecimal("50000.00"), 30));

        mockMvc.perform(get("/plans/" + plan.getId())
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(plan.getId()))
                .andExpect(jsonPath("$.name").value("Basic"));
    }

    @Test
    void getPlan_whenNotFound_returns404() throws Exception {
        mockMvc.perform(get("/plans/999999")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Plan not found"));
    }

    @Test
    void listPlans_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/plans"))
                .andExpect(status().isUnauthorized());
    }
}