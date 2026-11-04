package com.gms.members.api;

import com.gms.identity.controller.AuthTestHelper;
import com.gms.members.entity.Member;
import com.gms.members.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class UpdateMemberControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AuthTestHelper auth;

    @Test
    void updateMember_whenFullNameAndContactProvided_updatesBoth() throws Exception {
        Member member = new Member(999L, "DOC-TEST-UPD", "Original Name", "Original Contact");
        Long id = memberRepository.save(member).getId();

        String body = """
                {"fullName":"Updated Name","contact":"Updated Contact"}
                """;

        mockMvc.perform(patch("/members/" + id)
                        .header("Authorization", "Bearer " + auth.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.contact").value("Updated Contact"));

        Member updated = memberRepository.findById(id).orElseThrow();
        assertThat(updated.getFullName()).isEqualTo("Updated Name");
        assertThat(updated.getContact()).isEqualTo("Updated Contact");
    }

    @Test
    void updateMember_whenOnlyContactProvided_keepsFullName() throws Exception {
        Member member = new Member(998L, "DOC-TEST-UPD2", "Original Name", "Original Contact");
        Long id = memberRepository.save(member).getId();

        String body = """
                {"contact":"New Contact Only"}
                """;

        mockMvc.perform(patch("/members/" + id)
                        .header("Authorization", "Bearer " + auth.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Original Name"))
                .andExpect(jsonPath("$.contact").value("New Contact Only"));
    }

    @Test
    void updateMember_whenMemberDoesNotExist_returns404() throws Exception {
        String body = """
                {"fullName":"Does Not Matter"}
                """;

        mockMvc.perform(patch("/members/999999")
                        .header("Authorization", "Bearer " + auth.adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Member not found"));
    }
}