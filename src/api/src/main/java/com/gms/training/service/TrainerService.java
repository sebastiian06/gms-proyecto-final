package com.gms.training.service;

import com.gms.identity.dto.UserDto;
import com.gms.identity.entity.Role;
import com.gms.identity.service.IdentityFacade;
import com.gms.training.dto.CreateTrainerRequest;
import com.gms.training.entity.Trainer;
import com.gms.training.repository.TrainerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final IdentityFacade identityFacade;

    public TrainerService(TrainerRepository trainerRepository, IdentityFacade identityFacade) {
        this.trainerRepository = trainerRepository;
        this.identityFacade = identityFacade;
    }

    @Transactional
    public Trainer create(CreateTrainerRequest request) {
        return create(request.username(), request.password(), request.fullName());
    }

    @Transactional
    public Trainer create(String username, String password, String fullName) {
        UserDto user = identityFacade.createUser(username, password, Role.TRAINER);

        Trainer trainer = new Trainer(user.id(), fullName);
        return trainerRepository.save(trainer);
    }

    public List<Trainer> findAll() {
        return trainerRepository.findAll();
    }

    public Trainer findById(Long id) {
        return trainerRepository.findById(id)
                .orElseThrow(() -> new TrainerNotFoundException(id));
    }

    public Trainer findByUserId(Long userId) {
        return trainerRepository.findByUserId(userId)
                .orElseThrow(() -> new TrainerNotFoundException(userId));
    }

    public static class TrainerNotFoundException extends RuntimeException {
        public TrainerNotFoundException(Long id) {
            super("Trainer with id " + id + " not found");
        }
    }
}