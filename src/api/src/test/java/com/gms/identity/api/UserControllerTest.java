package com.gms.identity.api;

import com.gms.identity.controller.AuthTestHelper;
import com.gms.identity.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthTestHelper auth;

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_whenValidRequest_returns201WithUser() throws Exception {
        String token = auth.adminToken();

        String body = """
                {
                  "username": "juanp",
                  "password": "secret123",
                  "role": "MEMBER"
                }
                """;

        mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("juanp"))
                .andExpect(jsonPath("$.role").value("MEMBER"))
                .andExpect(jsonPath("$.active").value(true));

        assertThat(userRepository.existsByUsername("juanp")).isTrue();
    }

    @Test
    void createUser_withoutToken_returns401() throws Exception {
        String body = """
                {"username": "noauth", "password": "secret123", "role": "MEMBER"}
                """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}