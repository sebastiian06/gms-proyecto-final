package com.gms.identity.dto;

import com.gms.identity.entity.Role;
import com.gms.identity.entity.User;

public record UserDto(
        Long id,
        String username,
        Role role,
        Boolean active,
        Boolean isSystem
) {
    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getActive(),
                user.getIsSystem()
        );
    }
}