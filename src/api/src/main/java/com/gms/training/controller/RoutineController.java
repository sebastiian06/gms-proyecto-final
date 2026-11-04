
package com.gms.training.controller;

import com.gms.training.dto.CreateExerciseRequest;
import com.gms.training.dto.CreateRoutineRequest;
import com.gms.training.dto.ExerciseDto;
import com.gms.training.dto.RoutineDto;
import com.gms.training.dto.UpdateExerciseRequest;
import com.gms.training.dto.UpdateRoutineRequest;
import com.gms.training.entity.RoutineAssignment;
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
@RequestMapping("/routines")
public class RoutineController {

    private final TrainingFacade trainingFacade;

    public RoutineController(TrainingFacade trainingFacade) {
        this.trainingFacade = trainingFacade;
    }

    @PreAuthorize("hasRole('TRAINER')")
    @PostMapping("/trainers/{trainerId}")
    public ResponseEntity<RoutineDto> createRoutine(
            @PathVariable Long trainerId,
            @Valid @RequestBody CreateRoutineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(trainingFacade.createRoutine(trainerId, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/trainers/{trainerId}")
    public ResponseEntity<List<RoutineDto>> listRoutinesByTrainer(@PathVariable Long trainerId) {
        return ResponseEntity.ok(trainingFacade.findRoutinesByTrainer(trainerId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @GetMapping("/{id}")
    public ResponseEntity<RoutineDto> getRoutine(@PathVariable Long id) {
        return ResponseEntity.ok(trainingFacade.findRoutineById(id));
    }

    @PreAuthorize("hasRole('TRAINER')")
    @PutMapping("/{id}")
    public ResponseEntity<RoutineDto> updateRoutine(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoutineRequest request) {
        return ResponseEntity.ok(trainingFacade.updateRoutine(id, request));
    }

    @PreAuthorize("hasRole('TRAINER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateRoutine(@PathVariable Long id) {
        trainingFacade.deactivateRoutine(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    @PostMapping("/{id}/assign/{memberId}")
    public ResponseEntity<Void> assignRoutineToMember(
            @PathVariable Long id,
            @PathVariable Long memberId) {
        trainingFacade.assignRoutineToMember(id, memberId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER', 'MEMBER')")
    @GetMapping("/members/{memberId}")
    public ResponseEntity<RoutineDto> getMemberRoutine(@PathVariable Long memberId) {
        RoutineAssignment assignment = trainingFacade.findRoutineAssignmentByMember(memberId);
        return ResponseEntity.ok(trainingFacade.findRoutineById(assignment.getRoutine().getId()));
    }
}
