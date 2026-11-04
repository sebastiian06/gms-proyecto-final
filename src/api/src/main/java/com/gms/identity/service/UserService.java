
package com.gms.identity.service;

import com.gms.identity.entity.Role;
import com.gms.identity.entity.User;
import com.gms.identity.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(String username, String rawPassword, Role role) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUsernameException(username);
        }

        String passwordHash = passwordEncoder.encode(rawPassword);
        User user = new User(username, passwordHash, role);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public List<User> findAll(Role roleFilter, Boolean activeFilter) {
        return userRepository.findAll().stream()
                .filter(u -> roleFilter == null || u.getRole() == roleFilter)
                .filter(u -> activeFilter == null || u.getActive().equals(activeFilter))
                .toList();
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional
    public User update(Long id, String username, String password, Role role) {
        User user = findById(id);

        if (username != null && !username.equals(user.getUsername())) {
            if (userRepository.existsByUsername(username)) {
                throw new DuplicateUsernameException(username);
            }
            user.setUsername(username);
        }

        if (password != null && !password.isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(password));
        }

        if (role != null) {
            user.setRole(role);
        }

        return userRepository.save(user);
    }

    @Transactional
    public void activate(Long id) {
        User user = findById(id);
        if (user.getActive()) {
            throw new UserAlreadyActiveException(id);
        }
        user.activate();
        userRepository.save(user);
    }

    @Transactional
    public void deactivate(Long id) {
        User user = findById(id);

        if (user.getIsSystem()) {
            throw new SystemUserProtectedException();
        }

        if (!user.getActive()) {
            throw new UserAlreadyInactiveException(id);
        }

        // Cannot deactivate the last active ADMIN
        if (user.getRole() == Role.ADMIN) {
            long activeAdmins = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.ADMIN && u.getActive())
                    .count();
            if (activeAdmins <= 1) {
                throw new LastAdminException();
            }
        }

        user.deactivate();
        userRepository.save(user);
    }

    public static class DuplicateUsernameException extends RuntimeException {
        public DuplicateUsernameException(String username) {
            super("Username already exists: " + username);
        }
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(Long id) {
            super("User with id " + id + " not found");
        }
    }

    public static class UserAlreadyActiveException extends RuntimeException {
        public UserAlreadyActiveException(Long id) {
            super("User with id " + id + " is already active");
        }
    }

    public static class UserAlreadyInactiveException extends RuntimeException {
        public UserAlreadyInactiveException(Long id) {
            super("User with id " + id + " is already inactive");
        }
    }

    public static class SystemUserProtectedException extends RuntimeException {
        public SystemUserProtectedException() {
            super("The system admin cannot be deactivated");
        }
    }

    public static class LastAdminException extends RuntimeException {
        public LastAdminException() {
            super("Cannot deactivate the last active admin");
        }
    }
}
