package com.gms.training.repository;

import com.gms.training.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    Optional<Trainer> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}