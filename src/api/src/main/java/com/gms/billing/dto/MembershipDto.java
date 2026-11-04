package com.gms.billing.dto;

import com.gms.billing.entity.Membership;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MembershipDto(
        Long id,
        Long memberId,
        Long planId,
        BigDecimal lockedPrice,
        LocalDate startDate,
        LocalDate endDate,
        String status
) {
    public static MembershipDto from(Membership m) {
        return new MembershipDto(
                m.getId(),
                m.getMemberId(),
                m.getPlanId(),
                m.getLockedPrice(),
                m.getStartDate(),
                m.getEndDate(),
                m.getStatus().name()
        );
    }
}