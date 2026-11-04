package com.gms.training.dto;

import com.gms.training.entity.Trainer;

public record TrainerDto(
        Long id,
        Long userId,
        String fullName
) {
    public static TrainerDto from(Trainer t) {
        return new TrainerDto(t.getId(), t.getUserId(), t.getFullName());
    }
}