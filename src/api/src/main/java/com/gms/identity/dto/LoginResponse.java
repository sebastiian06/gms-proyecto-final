package com.gms.identity.dto;

import java.time.OffsetDateTime;

public record LoginResponse(
        String token,
        OffsetDateTime expiresAt,
        UserDto user
) {
}