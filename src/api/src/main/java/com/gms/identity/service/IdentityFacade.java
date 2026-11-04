
package com.gms.identity.service;

import com.gms.identity.dto.LoginResponse;
import com.gms.identity.dto.UserDto;
import com.gms.identity.entity.AccessCode;
import com.gms.identity.entity.Role;
import com.gms.identity.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class IdentityFacade {

    private final UserService userService;
    private final AuthService authService;
    private final AccessCodeService accessCodeService;

    public IdentityFacade(
            UserService userService,
            AuthService authService,
            AccessCodeService accessCodeService) {
        this.userService = userService;
        this.authService = authService;
        this.accessCodeService = accessCodeService;
    }

    public UserDto createUser(String username, String password, Role role) {
        User user = userService.createUser(username, password, role);
        return UserDto.from(user);
    }

    public LoginResponse login(String username, String password) {
        AuthService.AuthResult result = authService.login(username, password);
        return new LoginResponse(
                result.token(),
                result.expiresAt(),
                UserDto.from(result.user())
        );
    }

    @Transactional(readOnly = true)
    public User findByAccessCode(String code) {
        AccessCode accessCode = accessCodeService.findByCode(code);
        return accessCode.getUser();
    }

    @Transactional
    public String generateAccessCode(Long userId) {
        User user = userService.findById(userId);
        return accessCodeService.generateFor(user).getCode();
    }

    @Transactional
    public String regenerateAccessCode(Long userId) {
        User user = userService.findById(userId);
        return accessCodeService.regenerateFor(user).getCode();
    }

    @Transactional(readOnly = true)
    public Optional<String> findActiveAccessCode(Long userId) {
        return accessCodeService.findActiveCodeForUser(userId)
                .map(AccessCode::getCode);
    }

    @Transactional(readOnly = true)
    public Long findUserIdByUsername(String username) {
        return userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    @Transactional
    public UserDto updateUser(
            Long userId,
            String username,
            String password,
            Role role) {
        User user = userService.update(userId, username, password, role);
        return UserDto.from(user);
    }
}
