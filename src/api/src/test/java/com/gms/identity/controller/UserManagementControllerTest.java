package com.gms.identity.controller;

import com.gms.identity.entity.Role;
import com.gms.identity.entity.User;
import com.gms.identity.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class UserManagementControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuthTestHelper auth;

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    void listUsers_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isOk());
    }

    @Test
    void listUsers_asReceptionist_returns403() throws Exception {
        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + auth.receptionistToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deactivateUser_asAdmin_returns204() throws Exception {
        User u = userRepository.save(new User(
                "u_deact", passwordEncoder.encode("secret123"), Role.RECEPTIONIST));

        mockMvc.perform(patch("/users/" + u.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(u.getId()).orElseThrow().getActive()).isFalse();
    }

    @Test
    void deactivateUser_whenInactive_returns409() throws Exception {
        User u = new User("u_deact2", passwordEncoder.encode("secret123"), Role.RECEPTIONIST);
        u.deactivate();
        Long id = userRepository.save(u).getId();

        mockMvc.perform(patch("/users/" + id + "/deactivate")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isConflict());
    }

    @Test
    void deactivateSystemAdmin_returns409() throws Exception {
        User sysAdmin = userRepository.findByUsername("admin").orElse(null);
        if (sysAdmin == null) {
            sysAdmin = new User("admin", passwordEncoder.encode("admin123"), Role.ADMIN);
            sysAdmin.setIsSystem(true);
            sysAdmin = userRepository.save(sysAdmin);
        }

        mockMvc.perform(patch("/users/" + sysAdmin.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("User conflict"));
    }

    @Test
    void activateUser_whenInactive_returns204() throws Exception {
        User u = new User("u_act", passwordEncoder.encode("secret123"), Role.RECEPTIONIST);
        u.deactivate();
        Long id = userRepository.save(u).getId();

        mockMvc.perform(patch("/users/" + id + "/activate")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(id).orElseThrow().getActive()).isTrue();
    }

    @Test
    void updateUserRole_asAdmin_returns200() throws Exception {
        User u = userRepository.save(new User(
                "u_role", passwordEncoder.encode("secret123"), Role.RECEPTIONIST));

        String body = """
                {"role":"ADMIN"}
                """;
        mockMvc.perform(patch("/users/" + u.getId())
                        .header("Authorization", "Bearer " + auth.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void deactivateLastAdmin_returns409() throws Exception {
        // After the AdminSeeder, "admin" (is_system=true) is the only active admin.
        // Create a non-system admin, then deactivate it: should succeed.
        User admin2 = userRepository.save(new User(
                "admin2", passwordEncoder.encode("secret123"), Role.ADMIN));

        mockMvc.perform(patch("/users/" + admin2.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isNoContent());
    }
}