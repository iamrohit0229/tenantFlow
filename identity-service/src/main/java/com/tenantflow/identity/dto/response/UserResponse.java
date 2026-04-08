package com.tenantflow.identity.dto.response;

import com.tenantflow.identity.entity.Role;
import com.tenantflow.identity.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(

        UUID id,
        UUID tenantId,
        String email,
        String firstName,
        String lastName,
        Role role,
        boolean isActive,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    // Static factory — converts entity to response
    // NEVER expose entity directly in API!
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getTenantId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.isActive(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
