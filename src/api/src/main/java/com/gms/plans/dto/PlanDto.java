package com.gms.plans.dto;

import com.gms.plans.entity.Plan;

import java.math.BigDecimal;

public record PlanDto(
        Long id,
        String name,
        BigDecimal price,
        Integer durationDays,
        Boolean active
) {
    public static PlanDto from(Plan plan) {
        return new PlanDto(
                plan.getId(),
                plan.getName(),
                plan.getPrice(),
                plan.getDurationDays(),
                plan.getActive()
        );
    }
}