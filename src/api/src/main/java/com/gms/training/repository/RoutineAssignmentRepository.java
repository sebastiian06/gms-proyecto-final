package com.gms.training.repository;

import com.gms.training.entity.RoutineAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoutineAssignmentRepository extends JpaRepository<RoutineAssignment, Long> {

    Optional<RoutineAssignment> findByMemberId(Long memberId);

    boolean existsByRoutineIdAndMemberId(Long routineId, Long memberId);
}