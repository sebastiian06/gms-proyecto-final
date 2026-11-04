package com.gms.training.service;

import com.gms.training.dto.*;
import com.gms.training.entity.Exercise;
import com.gms.training.entity.Routine;
import com.gms.training.entity.RoutineAssignment;
import com.gms.training.entity.Trainer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TrainingFacade {

    private final TrainerService trainerService;
    private final RoutineService routineService;
    private final ExerciseService exerciseService;

    public TrainingFacade(TrainerService trainerService,
                          RoutineService routineService,
                          ExerciseService exerciseService) {
        this.trainerService = trainerService;
        this.routineService = routineService;
        this.exerciseService = exerciseService;
    }

    // Trainers
    public TrainerDto createTrainer(CreateTrainerRequest request) {
        return TrainerDto.from(trainerService.create(request));
    }

    public TrainerDto createTrainer(String username, String password, String fullName) {
        return TrainerDto.from(trainerService.create(username, password, fullName));
    }

    public List<TrainerDto> findAllTrainers() {
        return trainerService.findAll().stream()
                .map(TrainerDto::from)
                .collect(Collectors.toList());
    }

    public TrainerDto findTrainerById(Long id) {
        return TrainerDto.from(trainerService.findById(id));
    }

    public TrainerDto findTrainerByUserId(Long userId) {
        return TrainerDto.from(trainerService.findByUserId(userId));
    }

    // Routines
    public RoutineDto createRoutine(Long trainerId, CreateRoutineRequest request) {
        return RoutineDto.from(routineService.create(trainerId, request));
    }

    public List<RoutineDto> findRoutinesByTrainer(Long trainerId) {
        return routineService.findByTrainer(trainerId).stream()
                .map(RoutineDto::from)
                .collect(Collectors.toList());
    }

    public RoutineDto findRoutineById(Long id) {
        return RoutineDto.from(routineService.findById(id));
    }

    public RoutineDto updateRoutine(Long id, UpdateRoutineRequest request) {
        return RoutineDto.from(routineService.update(id, request));
    }

    public void deactivateRoutine(Long id) {
        routineService.deactivate(id);
    }

    public RoutineAssignment assignRoutineToMember(Long routineId, Long memberId) {
        return routineService.assignToMember(routineId, memberId);
    }

    public RoutineAssignment findRoutineAssignmentByMember(Long memberId) {
        return routineService.findAssignmentByMember(memberId);
    }

    // Exercises
    public ExerciseDto createExercise(Long routineId, CreateExerciseRequest request) {
        return ExerciseDto.from(exerciseService.create(routineId, request));
    }

    public List<ExerciseDto> findExercisesByRoutine(Long routineId) {
        return exerciseService.findByRoutine(routineId).stream()
                .map(ExerciseDto::from)
                .collect(Collectors.toList());
    }

    public ExerciseDto updateExercise(Long exerciseId, UpdateExerciseRequest request) {
        return ExerciseDto.from(exerciseService.update(exerciseId, request));
    }

    public void deleteExercise(Long exerciseId) {
        exerciseService.delete(exerciseId);
    }
}