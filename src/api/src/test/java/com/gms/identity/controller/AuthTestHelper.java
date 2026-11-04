package com.gms.identity.controller;

import com.gms.identity.entity.Role;
import com.gms.identity.entity.User;
import com.gms.identity.repository.UserRepository;
import com.gms.identity.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthTestHelper {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    public String adminToken() {
        return tokenFor("test-admin", Role.ADMIN);
    }

    public String receptionistToken() {
        return tokenFor("test-receptionist", Role.RECEPTIONIST);
    }

    public String trainerToken() {
        return tokenFor("test-trainer", Role.TRAINER);
    }

    public String memberToken() {
        return tokenFor("test-member", Role.MEMBER);
    }

    private String tokenFor(String username, Role role) {
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.save(new User(username, "$2a$10$dummy", role)));
        return jwtService.generateToken(user);
    }
}