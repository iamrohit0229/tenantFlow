package com.tenantflow.identity.service;

import com.tenantflow.identity.dto.request.LoginRequest;
import com.tenantflow.identity.dto.request.RefreshTokenRequest;
import com.tenantflow.identity.dto.request.RegisterRequest;
import com.tenantflow.identity.dto.response.AuthResponse;
import com.tenantflow.identity.dto.response.UserResponse;

public interface AuthService {

    // Register new user under a tenant
    UserResponse register(RegisterRequest request);

    // Login and get JWT tokens
    AuthResponse login(LoginRequest request);

    // Get new access token using refresh token
    AuthResponse refresh(RefreshTokenRequest request);

    // Logout and blacklist token
    void logout(String token);

    // Validate token — called by API Gateway
    boolean validateToken(String token);
}
