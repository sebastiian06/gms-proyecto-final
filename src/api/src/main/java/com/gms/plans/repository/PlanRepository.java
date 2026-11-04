package com.gms.plans.repository;

import com.gms.plans.entity.Plan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    Optional<Plan> findByNameAndActiveTrue(String name);

    boolean existsByNameAndActiveTrue(String name);

    Page<Plan> findByActive(Boolean active, Pageable pageable);
}