package com.gms.training.controller;

import com.gms.training.dto.CreateTrainerRequest;
import com.gms.training.dto.TrainerDto;
import com.gms.training.service.TrainingFacade;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/trainers")
public class TrainerController {

    private final TrainingFacade trainingFacade;

    public TrainerController(TrainingFacade trainingFacade) {
        this.trainingFacade = trainingFacade;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<TrainerDto> createTrainer(
            @Valid @RequestBody CreateTrainerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(trainingFacade.createTrainer(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'TRAINER')")
    @GetMapping
    public ResponseEntity<List<TrainerDto>> listTrainers() {
        return ResponseEntity.ok(trainingFacade.findAllTrainers());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'TRAINER')")
    @GetMapping("/{id}")
    public ResponseEntity<TrainerDto> getTrainer(@PathVariable Long id) {
        return ResponseEntity.ok(trainingFacade.findTrainerById(id));
    }
}