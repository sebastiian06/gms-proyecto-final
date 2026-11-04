package com.gms.identity.api;

import com.gms.identity.entity.Role;
import com.gms.identity.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        com.gms.identity.entity.User user =
                new com.gms.identity.entity.User(
                        "juanp",
                        passwordEncoder.encode("secret123"),
                        Role.MEMBER);
        userRepository.save(user);
    }

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    void login_whenValidCredentials_returnsToken() throws Exception {
        String body = """
                {"username": "juanp", "password": "secret123"}
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.expiresAt").isString())
                .andExpect(jsonPath("$.user.username").value("juanp"))
                .andExpect(jsonPath("$.user.role").value("MEMBER"));
    }

    @Test
    void login_whenWrongPassword_returns401() throws Exception {
        String body = """
                {"username": "juanp", "password": "wrong"}
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Invalid credentials"));
    }

    @Test
    void login_whenUsernameDoesNotExist_returns401() throws Exception {
        String body = """
                {"username": "nobody", "password": "secret123"}
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}