package com.gms.training.repository;

import com.gms.training.entity.Routine;
import com.gms.training.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoutineRepository extends JpaRepository<Routine, Long> {

    List<Routine> findByTrainerAndActiveTrue(Trainer trainer);

    List<Routine> findByActiveTrue();
}