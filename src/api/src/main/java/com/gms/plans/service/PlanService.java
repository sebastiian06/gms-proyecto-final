package com.gms.plans.service;

import com.gms.plans.entity.Plan;
import com.gms.plans.repository.PlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

@Service
public class PlanService {

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Transactional
    public Plan create(String name, BigDecimal price, Integer durationDays) {
        if (planRepository.existsByNameAndActiveTrue(name)) {
            throw new DuplicatePlanNameException(name);
        }
        Plan plan = new Plan(name, price, durationDays);
        return planRepository.save(plan);
    }

    public static class DuplicatePlanNameException extends RuntimeException {
        public DuplicatePlanNameException(String name) {
            super("An active plan with name '" + name + "' already exists");
        }
    }

    public static class PlanNotFoundException extends RuntimeException {
        public PlanNotFoundException(Long id) {
            super("Plan with id " + id + " not found");
        }
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Plan> findAll(
            Boolean activeFilter,
            org.springframework.data.domain.Pageable pageable) {

        if (activeFilter == null) {
            return planRepository.findAll(pageable);
        }
        return planRepository.findByActive(activeFilter, pageable);
    }

    @Transactional(readOnly = true)
    public Plan findById(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new PlanNotFoundException(id));
    }

    @Transactional
    public Plan update(Long id, String name, BigDecimal price, Integer durationDays) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new PlanNotFoundException(id));

        // If name changed, ensure no other active plan has it
        if (!plan.getName().equals(name)
                && planRepository.existsByNameAndActiveTrue(name)) {
            throw new DuplicatePlanNameException(name);
        }

        plan.setName(name);
        plan.setPrice(price);
        plan.setDurationDays(durationDays);
        return planRepository.save(plan);
    }

    @Transactional
    public void deactivate(Long id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new PlanNotFoundException(id));

        if (!plan.getActive()) {
            throw new AlreadyInactiveException(id);
        }

        plan.deactivate();
        planRepository.save(plan);
    }

    public static class AlreadyInactiveException extends RuntimeException {
        public AlreadyInactiveException(Long id) {
            super("Plan with id " + id + " is already inactive");
        }
    }

    @Transactional
    public void activate(Long id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new PlanNotFoundException(id));

        if (plan.getActive()) {
            throw new AlreadyActiveException(id);
        }

        plan.activate();
        planRepository.save(plan);
    }

    public static class AlreadyActiveException extends RuntimeException {
        public AlreadyActiveException(Long id) {
            super("Plan with id " + id + " is already active");
        }
    }
}