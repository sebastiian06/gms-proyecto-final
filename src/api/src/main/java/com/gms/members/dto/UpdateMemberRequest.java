package com.gms.members.dto;

import com.gms.billing.entity.Payment;

public record UpdateMemberRequest(
    String username,
    String password,
    String documentNumber,
    String fullName,
    String contact,
    Long planId,
    Long trainerId
) {}