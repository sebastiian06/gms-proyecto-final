package com.gms.training.service;

import com.gms.training.dto.CreateExerciseRequest;
import com.gms.training.dto.UpdateExerciseRequest;
import com.gms.training.entity.Exercise;
import com.gms.training.entity.Routine;
import com.gms.training.repository.ExerciseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final RoutineService routineService;

    public ExerciseService(ExerciseRepository exerciseRepository, RoutineService routineService) {
        this.exerciseRepository = exerciseRepository;
        this.routineService = routineService;
    }

    @Transactional
    public Exercise create(Long routineId, CreateExerciseRequest request) {
        Routine routine = routineService.findById(routineId);
        int nextOrder = exerciseRepository.countByRoutine(routine);

        Exercise exercise = new Exercise(
                routine,
                request.name(),
                request.sets(),
                request.repetitions(),
                nextOrder
        );
        return exerciseRepository.save(exercise);
    }

    public List<Exercise> findByRoutine(Long routineId) {
        Routine routine = routineService.findById(routineId);
        return exerciseRepository.findByRoutineOrderByDisplayOrderAsc(routine);
    }

    @Transactional
    public Exercise update(Long exerciseId, UpdateExerciseRequest request) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

        exercise.setName(request.name());
        exercise.setSets(request.sets());
        exercise.setRepetitions(request.repetitions());
        return exerciseRepository.save(exercise);
    }

    @Transactional
    public void delete(Long exerciseId) {
        if (!exerciseRepository.existsById(exerciseId)) {
            throw new ExerciseNotFoundException(exerciseId);
        }
        exerciseRepository.deleteById(exerciseId);
    }

    public static class ExerciseNotFoundException extends RuntimeException {
        public ExerciseNotFoundException(Long id) {
            super("Exercise with id " + id + " not found");
        }
    }
}