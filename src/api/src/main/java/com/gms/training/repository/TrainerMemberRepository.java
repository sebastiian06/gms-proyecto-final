package com.gms.training.repository;

import com.gms.training.entity.TrainerMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrainerMemberRepository extends JpaRepository<TrainerMember, Long> {

    Optional<TrainerMember> findByMemberId(Long memberId);

    List<TrainerMember> findByTrainerId(Long trainerId);

    boolean existsByTrainerIdAndMemberId(Long trainerId, Long memberId);
}