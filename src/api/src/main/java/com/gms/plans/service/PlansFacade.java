package com.gms.plans.service;

import com.gms.plans.dto.PlanDto;
import com.gms.plans.entity.Plan;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PatchMapping;

import java.math.BigDecimal;

@Component
public class PlansFacade {

    private final PlanService planService;

    public PlansFacade(PlanService planService) {
        this.planService = planService;
    }

    public PlanDto create(String name, BigDecimal price, Integer durationDays) {
        Plan plan = planService.create(name, price, durationDays);
        return PlanDto.from(plan);
    }

    public Page<PlanDto> findAll(Boolean activeFilter, Pageable pageable) {
        return planService.findAll(activeFilter, pageable).map(PlanDto::from);
    }

    public PlanDto findById(Long id) {
        return PlanDto.from(planService.findById(id));
    }

    public PlanDto update(Long id, String name, BigDecimal price, Integer durationDays) {
        Plan plan = planService.update(id, name, price, durationDays);
        return PlanDto.from(plan);
    }

    public void deactivate(Long id) {
        planService.deactivate(id);
    }

    public void activate(Long id) {
        planService.activate(id);
    }
}