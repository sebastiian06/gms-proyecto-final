package com.gms.identity.repository;

import com.gms.identity.entity.AccessCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccessCodeRepository extends JpaRepository<AccessCode, Long> {

    Optional<AccessCode> findByCodeAndActiveTrue(String code);

    boolean existsByCodeAndActiveTrue(String code);

    Optional<AccessCode> findFirstByUser_IdAndActiveTrue(Long userId);
}