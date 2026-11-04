package com.gms.training.repository;

import com.gms.training.entity.Exercise;
import com.gms.training.entity.Routine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    List<Exercise> findByRoutineOrderByDisplayOrderAsc(Routine routine);

    int countByRoutine(Routine routine);
}