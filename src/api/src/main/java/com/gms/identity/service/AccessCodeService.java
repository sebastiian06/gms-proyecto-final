package com.gms.identity.service;

import com.gms.identity.entity.AccessCode;
import com.gms.identity.entity.User;
import com.gms.identity.repository.AccessCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

@Service
public class AccessCodeService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int CODE_LENGTH = 4;

    private final AccessCodeRepository accessCodeRepository;

    public AccessCodeService(AccessCodeRepository accessCodeRepository) {
        this.accessCodeRepository = accessCodeRepository;
    }

    @Transactional
    public AccessCode generateFor(User user) {
        // Deactivate any existing active code
        accessCodeRepository.findAll().stream()
                .filter(c -> c.getUser().getId().equals(user.getId()) && c.getActive())
                .forEach(c -> {
                    c.deactivate();
                    accessCodeRepository.save(c);
                });

        String code = generateUniqueCode();
        AccessCode accessCode = new AccessCode(user, code);
        return accessCodeRepository.save(accessCode);
    }

    @Transactional
    public AccessCode regenerateFor(User user) {
        return generateFor(user); // same logic: deactivate old, create new
    }

    public AccessCode findByCode(String code) {
        return accessCodeRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new AccessCodeNotFoundException(code));
    }

    @Transactional(readOnly = true)
    public Optional<AccessCode> findActiveCodeForUser(Long userId) {
        return accessCodeRepository.findFirstByUser_IdAndActiveTrue(userId);
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < 100; attempt++) {
            int number = RANDOM.nextInt(9000) + 1000; // 1000-9999
            String code = String.format("%04d", number);
            if (!accessCodeRepository.existsByCodeAndActiveTrue(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Could not generate a unique access code");
    }

    public static class AccessCodeNotFoundException extends RuntimeException {
        public AccessCodeNotFoundException(String code) {
            super("Access code '" + code + "' not found");
        }
    }
}