package com.gms.platform.config;

import com.gms.identity.entity.Role;
import com.gms.identity.entity.User;
import com.gms.identity.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${gms.seed.admin.username:admin}")
    private String adminUsername;

    @Value("${gms.seed.admin.password:admin123}")
    private String adminPassword;

    public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. Admin (system user — no se puede desactivar)
        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = new User(adminUsername, passwordEncoder.encode(adminPassword), Role.ADMIN);
            admin.setIsSystem(true);
            userRepository.save(admin);
            log.info("Seeded system admin '{}'", adminUsername);
        } else {
            log.info("System admin '{}' already exists, skipping", adminUsername);
        }

        // 2. Receptionist (test user)
        seedUser("reception1", "secret123", Role.RECEPTIONIST);

        // 3. Trainer (test user)
        seedUser("trainer1", "secret123", Role.TRAINER);

        // 4. Member (test user) — solo crea el identity_user, no el member profile
        seedUser("member1", "secret123", Role.MEMBER);
    }

    private void seedUser(String username, String password, Role role) {
        if (userRepository.existsByUsername(username)) {
            log.info("User '{}' already exists, skipping", username);
            return;
        }
        User user = new User(username, passwordEncoder.encode(password), role);
        userRepository.save(user);
        log.info("Seeded test user '{}' with role {}", username, role);
    }
}