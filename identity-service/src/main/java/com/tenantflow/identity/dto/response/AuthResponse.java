package com.tenantflow.identity.dto.response;

import com.tenantflow.identity.entity.Role;

import java.util.UUID;

public record AuthResponse(

        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        UUID userId,
        UUID tenantId,
        String email,
        String firstName,
        String lastName,
        Role role
) {
    // Static factory method for clean construction
    public static AuthResponse of(
            String accessToken,
            String refreshToken,
            Long expiresIn,
            UUID userId,
            UUID tenantId,
            String email,
            String firstName,
            String lastName,
            Role role
    ) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                expiresIn,
                userId,
                tenantId,
                email,
                firstName,
                lastName,
                role
        );
    }
}
