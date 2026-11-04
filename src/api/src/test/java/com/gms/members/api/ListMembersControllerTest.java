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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class ListMembersControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberRepository memberRepository;
    @Autowired private AuthTestHelper auth;

    @Test
    void listMembers_whenAuthenticated_returnsPage() throws Exception {
        memberRepository.save(new Member(900L, "DOC-LM-1", "Alice", "111"));
        memberRepository.save(new Member(901L, "DOC-LM-2", "Bob", "222"));

        mockMvc.perform(get("/members")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page.totalElements").value(2));
    }

    @Test
    void listMembers_whenFilteredByActive_returnsOnlyActive() throws Exception {
        memberRepository.save(new Member(900L, "DOC-LM-3", "Active", "111"));
        Member inactive = new Member(901L, "DOC-LM-4", "Inactive", "222");
        inactive.deactivate();
        memberRepository.save(inactive);

        mockMvc.perform(get("/members?active=true")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].fullName").value("Active"));
    }

    @Test
    void getMember_whenExists_returnsMember() throws Exception {
        Member m = memberRepository.save(new Member(900L, "DOC-LM-5", "Alice", "111"));

        mockMvc.perform(get("/members/" + m.getId())
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(m.getId()))
                .andExpect(jsonPath("$.fullName").value("Alice"));
    }

    @Test
    void getMember_whenNotFound_returns404() throws Exception {
        mockMvc.perform(get("/members/999999")
                        .header("Authorization", "Bearer " + auth.adminToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Member not found"));
    }

    @Test
    void listMembers_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/members"))
                .andExpect(status().isUnauthorized());
    }
}