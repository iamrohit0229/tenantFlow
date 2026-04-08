package com.tenantflow.identity.controller;

import com.tenantflow.identity.dto.request.LoginRequest;
import com.tenantflow.identity.dto.request.RefreshTokenRequest;
import com.tenantflow.identity.dto.request.RegisterRequest;
import com.tenantflow.identity.dto.response.AuthResponse;
import com.tenantflow.identity.dto.response.UserResponse;
import com.tenantflow.identity.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Auth endpoints for register, login, logout")
public class AuthController {

    private final AuthService authService;

    // --- Register ---

    @PostMapping("/register")
    @Operation(summary = "Register a new user under a tenant")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("Register request for email: {}", request.email());
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --- Login ---

    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT access + refresh tokens")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Login request for email: {}", request.email());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // --- Refresh Token ---

    @PostMapping("/refresh")
    @Operation(summary = "Get new access token using refresh token")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        log.info("Refresh token request received");
        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    // --- Logout ---

    @PostMapping("/logout")
    @Operation(summary = "Logout and blacklist current token")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authHeader) {

        // Extract token from Bearer header
        String token = authHeader.substring(7);
        authService.logout(token);

        return ResponseEntity.ok(
                Map.of("message", "Logged out successfully"));
    }

    // --- Validate Token ---
    // Called by API Gateway to verify token is valid

    @GetMapping("/validate")
    @Operation(summary = "Validate JWT token — used by API Gateway")
    public ResponseEntity<Map<String, Object>> validateToken(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        boolean isValid = authService.validateToken(token);

        if (isValid) {
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "message", "Token is valid"
            ));
        }

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "valid", false,
                        "message", "Token is invalid or expired"
                ));
    }
}
