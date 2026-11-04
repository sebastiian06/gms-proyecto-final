package com.gms.training.dto;

import com.gms.training.entity.Exercise;

public record ExerciseDto(
        Long id,
        Long routineId,
        String name,
        Integer sets,
        Integer repetitions,
        Integer displayOrder
) {
    public static ExerciseDto from(Exercise e) {
        return new ExerciseDto(
                e.getId(),
                e.getRoutine().getId(),
                e.getName(),
                e.getSets(),
                e.getRepetitions(),
                e.getDisplayOrder()
        );
    }
}