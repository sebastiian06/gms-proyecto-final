package com.gms.members.repository;

import com.gms.members.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByDocumentNumber(String documentNumber);

    boolean existsByUserId(Long userId);

    Optional<Member> findByUserId(Long userId);

    Page<Member> findByActive(Boolean active, Pageable pageable);
}