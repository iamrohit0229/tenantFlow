package com.tenantflow.identity.controller;

import com.tenantflow.identity.dto.request.RegisterRequest;
import com.tenantflow.identity.dto.response.UserResponse;
import com.tenantflow.identity.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    // --- Get All Users For Tenant ---

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get all users for a tenant")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsersByTenant(
            @PathVariable UUID tenantId) {

        log.info("Get all users request for tenant: {}", tenantId);
        List<UserResponse> users = userService.getAllUsersByTenant(tenantId);
        return ResponseEntity.ok(users);
    }

    // --- Get User By Id ---

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable UUID userId) {

        log.info("Get user request for id: {}", userId);
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    // --- Update User ---

    @PutMapping("/{userId}")
    @Operation(summary = "Update user details")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID userId,
            @RequestBody RegisterRequest request) {

        log.info("Update user request for id: {}", userId);
        UserResponse updatedUser = userService.updateUser(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    // --- Deactivate User ---

    @DeleteMapping("/{userId}")
    @Operation(summary = "Deactivate a user — soft delete")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> deactivateUser(
            @PathVariable UUID userId) {

        log.info("Deactivate user request for id: {}", userId);
        userService.deactivateUser(userId);

        return ResponseEntity.ok(
                Map.of("message", "User deactivated successfully"));
    }
}
