package com.gms.identity.controller;

import com.gms.identity.dto.CreateUserRequest;
import com.gms.identity.dto.UpdateUserRequest;
import com.gms.identity.dto.UserDto;
import com.gms.identity.entity.Role;
import com.gms.identity.entity.User;
import com.gms.identity.service.AccessCodeService;
import com.gms.identity.service.IdentityFacade;
import com.gms.identity.service.UserService;
import com.gms.training.dto.TrainerDto;
import com.gms.training.service.TrainingFacade;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final IdentityFacade identityFacade;
    private final UserService userService;
    private final AccessCodeService accessCodeService;
    private final TrainingFacade trainingFacade;

    public UserController(IdentityFacade identityFacade,
                          UserService userService,
                          AccessCodeService accessCodeService,
                          TrainingFacade trainingFacade) {
        this.identityFacade = identityFacade;
        this.userService = userService;
        this.accessCodeService = accessCodeService;
        this.trainingFacade = trainingFacade;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDto user;
        if (request.role() == Role.TRAINER) {
            // A TRAINER user must also have a trainer profile (same transaction)
            String trainerName = (request.fullName() != null && !request.fullName().isBlank())
                    ? request.fullName()
                    : request.username();
            TrainerDto trainer = trainingFacade.createTrainer(
                    request.username(), request.password(), trainerName);
            user = UserDto.from(userService.findById(trainer.userId()));
        } else {
            user = identityFacade.createUser(
                    request.username(),
                    request.password(),
                    request.role()
            );
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserDto>> listUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean active) {
        return ResponseEntity.ok(
                userService.findAll(role, active).stream()
                        .map(UserDto::from)
                        .toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(UserDto.from(userService.findById(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        User user = userService.update(id, request.username(), request.password(), request.role());
        return ResponseEntity.ok(UserDto.from(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        userService.activate(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @PostMapping("/{id}/regenerate-access-code")
    public ResponseEntity<Map<String, String>> regenerateAccessCode(@PathVariable Long id) {
        User user = userService.findById(id);
        var newCode = accessCodeService.regenerateFor(user);
        return ResponseEntity.ok(Map.of("accessCode", newCode.getCode()));
    }
}