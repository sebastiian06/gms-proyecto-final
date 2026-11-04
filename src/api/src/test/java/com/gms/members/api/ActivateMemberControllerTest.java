package com.gms.members.api;

import com.gms.identity.controller.AuthTestHelper;
import com.gms.members.entity.Member;
import com.gms.members.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
class ActivateMemberControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AuthTestHelper auth;

    @Test
    void activateMember_whenInactive_returns204() throws Exception {
        Member m = new Member(900L, "DOC-ACT-1", "Test", "123");
        m.deactivate();
        Long id = memberRepository.save(m).getId();

        mockMvc.perform(patch("/members/" + id + "/activate")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isNoContent());

        assertThat(memberRepository.findById(id).orElseThrow().getActive()).isTrue();
    }

    @Test
    void activateMember_whenAlreadyActive_returns409() throws Exception {
        Long id = memberRepository.save(new Member(900L, "DOC-ACT-2", "Test", "123")).getId();

        mockMvc.perform(patch("/members/" + id + "/activate")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Member already active"));
    }

    @Test
    void activateMember_whenNotFound_returns404() throws Exception {
        mockMvc.perform(patch("/members/999999/activate")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isNotFound());
    }
}