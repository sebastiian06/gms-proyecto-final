package com.gms.training.controller;

import com.gms.training.dto.CreateExerciseRequest;
import com.gms.training.dto.ExerciseDto;
import com.gms.training.dto.UpdateExerciseRequest;
import com.gms.training.service.TrainingFacade;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/routines/{routineId}/exercises")
public class ExerciseController {

    private final TrainingFacade trainingFacade;

    public ExerciseController(TrainingFacade trainingFacade) {
        this.trainingFacade = trainingFacade;
    }

    @PreAuthorize("hasRole('TRAINER')")
    @PostMapping
    public ResponseEntity<ExerciseDto> createExercise(
            @PathVariable Long routineId,
            @Valid @RequestBody CreateExerciseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(trainingFacade.createExercise(routineId, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'MEMBER')")
    @GetMapping
    public ResponseEntity<List<ExerciseDto>> listExercises(@PathVariable Long routineId) {
        return ResponseEntity.ok(trainingFacade.findExercisesByRoutine(routineId));
    }

    @PreAuthorize("hasRole('TRAINER')")
    @PutMapping("/{exerciseId}")
    public ResponseEntity<ExerciseDto> updateExercise(
            @PathVariable Long routineId,
            @PathVariable Long exerciseId,
            @Valid @RequestBody UpdateExerciseRequest request) {
        return ResponseEntity.ok(trainingFacade.updateExercise(exerciseId, request));
    }

    @PreAuthorize("hasRole('TRAINER')")
    @DeleteMapping("/{exerciseId}")
    public ResponseEntity<Void> deleteExercise(
            @PathVariable Long routineId,
            @PathVariable Long exerciseId) {
        trainingFacade.deleteExercise(exerciseId);
        return ResponseEntity.noContent().build();
    }
}