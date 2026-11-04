package com.gms.attendance.controller;

import com.gms.attendance.entity.AttendanceRecord;
import com.gms.attendance.repository.AttendanceRepository;
import com.gms.identity.entity.AccessCode;
import com.gms.identity.entity.Role;
import com.gms.identity.entity.User;
import com.gms.identity.repository.AccessCodeRepository;
import com.gms.identity.repository.UserRepository;
import com.gms.identity.service.AccessCodeService;
import com.gms.members.entity.Member;
import com.gms.members.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class AttendanceControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AccessCodeService accessCodeService;
    @Autowired private AccessCodeRepository accessCodeRepository;

    private String validCode;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(new User("member_kiosk", "$2a$10$dummy", Role.MEMBER));
        Member member = memberRepository.save(new Member(user.getId(), "DOC-KIOSK", "Test Kiosk", "123"));
        AccessCode code = accessCodeService.generateFor(user);
        validCode = code.getCode();
    }

    @AfterEach
    void cleanup() {
        attendanceRepository.deleteAll();
        accessCodeRepository.deleteAll();
        memberRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerEntry_whenValid_returns201AndCreatesRecord() throws Exception {
        String body = "{\"accessCode\":\"" + validCode + "\"}";

        mockMvc.perform(post("/attendance/entries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated());

        assertThat(attendanceRepository.findAll()).hasSize(1);
        assertThat(attendanceRepository.findAll().get(0).getDirection())
                .isEqualTo(AttendanceRecord.Direction.IN);
    }

    @Test
    void registerEntry_whenAlreadyCheckedIn_returns409() throws Exception {
        String body = "{\"accessCode\":\"" + validCode + "\"}";

        // First entry
        mockMvc.perform(post("/attendance/entries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated());

        // Second entry (should fail)
        mockMvc.perform(post("/attendance/entries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void registerExit_whenCheckedIn_returns201() throws Exception {
        String entryBody = "{\"accessCode\":\"" + validCode + "\"}";
        String exitBody = "{\"accessCode\":\"" + validCode + "\"}";

        // Check in
        mockMvc.perform(post("/attendance/entries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(entryBody))
                .andExpect(status().isCreated());

        // Check out
        mockMvc.perform(post("/attendance/exits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(exitBody))
                .andExpect(status().isCreated());

        assertThat(attendanceRepository.findAll()).hasSize(2);
        assertThat(attendanceRepository.findAll().get(1).getDirection())
                .isEqualTo(AttendanceRecord.Direction.OUT);
    }

    @Test
    void registerExit_whenNotCheckedIn_returns409() throws Exception {
        String exitBody = "{\"accessCode\":\"" + validCode + "\"}";

        mockMvc.perform(post("/attendance/exits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(exitBody))
                .andExpect(status().isConflict());
    }

    @Test
    void registerEntry_whenInvalidCode_returns404() throws Exception {
        String body = "{\"accessCode\":\"9999\"}";

        mockMvc.perform(post("/attendance/entries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isNotFound());
    }
}