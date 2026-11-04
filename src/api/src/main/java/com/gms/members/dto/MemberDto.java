package com.gms.members.dto;

import com.gms.members.entity.Member;

public record MemberDto(
        Long id,
        Long userId,
        String documentNumber,
        String fullName,
        String contact,
        Boolean active,
        Long trainerId,
        String trainerName,
        String accessCode
) {
    public static MemberDto from(Member m) {
        return from(m, null, null, null);
    }

    public static MemberDto from(Member m, String accessCode) {
        return from(m, null, null, accessCode);
    }

    public static MemberDto from(Member m, Long trainerId, String trainerName) {
        return from(m, trainerId, trainerName, null);
    }

    public static MemberDto from(Member m, Long trainerId, String trainerName, String accessCode) {
        return new MemberDto(
                m.getId(),
                m.getUserId(),
                m.getDocumentNumber(),
                m.getFullName(),
                m.getContact(),
                m.getActive(),
                trainerId,
                trainerName,
                accessCode
        );
    }
}