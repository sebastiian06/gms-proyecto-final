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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class DeactivateMemberControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AuthTestHelper auth;

    @Test
    void deactivateMember_whenActive_returns204() throws Exception {
        Member member = new Member(999L, "DOC-DEACT-1", "Test", "123");
        Long id = memberRepository.save(member).getId();

        mockMvc.perform(delete("/members/" + id)
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isNoContent());

        Member updated = memberRepository.findById(id).orElseThrow();
        assertThat(updated.getActive()).isFalse();
    }

    @Test
    void deactivateMember_whenAlreadyInactive_returns409() throws Exception {
        Member member = new Member(998L, "DOC-DEACT-2", "Test", "123");
        member.deactivate();
        Long id = memberRepository.save(member).getId();

        mockMvc.perform(delete("/members/" + id)
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Member already inactive"));
    }

    @Test
    void deactivateMember_whenDoesNotExist_returns404() throws Exception {
        mockMvc.perform(delete("/members/999999")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Member not found"));
    }
}