package com.gms.plans.controller;

import com.gms.plans.dto.CreatePlanRequest;
import com.gms.plans.dto.PlanDto;
import com.gms.plans.dto.UpdatePlanRequest;
import com.gms.plans.service.PlansFacade;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/plans")
public class PlanController {

    private final PlansFacade plansFacade;

    public PlanController(PlansFacade plansFacade) {
        this.plansFacade = plansFacade;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<PlanDto> createPlan(@Valid @RequestBody CreatePlanRequest request) {
        PlanDto plan = plansFacade.create(
                request.name(),
                request.price(),
                request.durationDays()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(plan);
    }

    @GetMapping
    public ResponseEntity<Page<PlanDto>> listPlans(
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by("name").ascending());

        return ResponseEntity.ok(plansFacade.findAll(active, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanDto> getPlan(@PathVariable Long id) {
        return ResponseEntity.ok(plansFacade.findById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PlanDto> updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePlanRequest request) {
        PlanDto plan = plansFacade.update(
                id, request.name(), request.price(), request.durationDays());
        return ResponseEntity.ok(plan);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivatePlan(@PathVariable Long id) {
        plansFacade.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activatePlan(@PathVariable Long id) {
        plansFacade.activate(id);
        return ResponseEntity.noContent().build();
    }
}