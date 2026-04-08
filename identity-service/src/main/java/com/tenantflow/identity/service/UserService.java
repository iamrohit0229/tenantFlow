package com.tenantflow.identity.service;

import com.tenantflow.identity.dto.request.RegisterRequest;
import com.tenantflow.identity.dto.response.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    // Get all users for a tenant
    List<UserResponse> getAllUsersByTenant(UUID tenantId);

    // Get single user by id
    UserResponse getUserById(UUID userId);

    // Update user details
    UserResponse updateUser(UUID userId, RegisterRequest request);

    // Deactivate user — soft delete
    void deactivateUser(UUID userId);
}
