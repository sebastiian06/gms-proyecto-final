package com.gms.training.dto;

import com.gms.training.entity.Routine;

public record RoutineDto(
        Long id,
        Long trainerId,
        String trainerName,
        String name,
        Boolean active
) {
    public static RoutineDto from(Routine r) {
        return new RoutineDto(
                r.getId(),
                r.getTrainer().getId(),
                r.getTrainer().getFullName(),
                r.getName(),
                r.getActive()
        );
    }
}