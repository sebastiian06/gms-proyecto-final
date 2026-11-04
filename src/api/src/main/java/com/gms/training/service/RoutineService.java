
package com.gms.training.service;

import com.gms.training.dto.CreateRoutineRequest;
import com.gms.training.dto.UpdateRoutineRequest;
import com.gms.training.entity.Routine;
import com.gms.training.entity.RoutineAssignment;
import com.gms.training.entity.Trainer;
import com.gms.training.repository.RoutineAssignmentRepository;
import com.gms.training.repository.RoutineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final RoutineAssignmentRepository assignmentRepository;
    private final TrainerService trainerService;

    public RoutineService(RoutineRepository routineRepository,
                          RoutineAssignmentRepository assignmentRepository,
                          TrainerService trainerService) {
        this.routineRepository = routineRepository;
        this.assignmentRepository = assignmentRepository;
        this.trainerService = trainerService;
    }

    @Transactional
    public Routine create(Long trainerId, CreateRoutineRequest request) {
        Trainer trainer = trainerService.findById(trainerId);
        Routine routine = new Routine(trainer, request.name());
        return routineRepository.save(routine);
    }

    public List<Routine> findByTrainer(Long trainerId) {
        Trainer trainer = trainerService.findById(trainerId);
        return routineRepository.findByTrainerAndActiveTrue(trainer);
    }

    public Routine findById(Long id) {
        return routineRepository.findById(id)
                .orElseThrow(() -> new RoutineNotFoundException(id));
    }

    @Transactional
    public Routine update(Long id, UpdateRoutineRequest request) {
        Routine routine = findById(id);
        routine.setName(request.name());
        return routineRepository.save(routine);
    }

    @Transactional
    public void deactivate(Long id) {
        Routine routine = findById(id);
        routine.deactivate();
        routineRepository.save(routine);
    }

    @Transactional
    public RoutineAssignment assignToMember(Long routineId, Long memberId) {
        Routine routine = findById(routineId);

        // If member already has a routine, remove the old assignment
        assignmentRepository.findByMemberId(memberId)
                .ifPresent(assignmentRepository::delete);

        assignmentRepository.flush();

        RoutineAssignment assignment = new RoutineAssignment(routine, memberId);
        return assignmentRepository.save(assignment);
    }

    public RoutineAssignment findAssignmentByMember(Long memberId) {
        return assignmentRepository.findByMemberId(memberId)
                .orElseThrow(() -> new NoRoutineAssignedException(memberId));
    }

    public static class RoutineNotFoundException extends RuntimeException {
        public RoutineNotFoundException(Long id) {
            super("Routine with id " + id + " not found");
        }
    }

    public static class NoRoutineAssignedException extends RuntimeException {
        public NoRoutineAssignedException(Long memberId) {
            super("Member with id " + memberId + " has no routine assigned");
        }
    }
}
