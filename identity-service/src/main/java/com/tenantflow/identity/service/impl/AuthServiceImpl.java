package com.tenantflow.identity.service.impl;

import com.tenantflow.identity.dto.request.LoginRequest;
import com.tenantflow.identity.dto.request.RefreshTokenRequest;
import com.tenantflow.identity.dto.request.RegisterRequest;
import com.tenantflow.identity.dto.response.AuthResponse;
import com.tenantflow.identity.dto.response.UserResponse;
import com.tenantflow.identity.entity.RefreshToken;
import com.tenantflow.identity.entity.Role;
import com.tenantflow.identity.entity.User;
import com.tenantflow.identity.exception.AccountLockedException;
import com.tenantflow.identity.exception.InvalidTokenException;
import com.tenantflow.identity.exception.UserAlreadyExistsException;
import com.tenantflow.identity.exception.UserNotFoundException;
import com.tenantflow.identity.kafka.AuthEventProducer;
import com.tenantflow.identity.repository.RefreshTokenRepository;
import com.tenantflow.identity.repository.UserRepository;
import com.tenantflow.identity.service.AuthService;
import com.tenantflow.identity.service.JwtService;
import com.tenantflow.identity.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AuthEventProducer authEventProducer;

    @Value("${jwt.refresh-expiration-ms}")
    private Long refreshExpirationMs;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;

    // --- Register ---

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Registering user: {} for tenant: {}",
                request.email(), request.tenantId());

        // Check if email already exists in this tenant
        if (userRepository.existsByEmailAndTenantId(
                request.email(), request.tenantId())) {
            throw new UserAlreadyExistsException(
                    "User with email " + request.email() +
                            " already exists in this tenant");
        }

        // Default role to TENANT_USER if not provided
        Role role = request.role() != null ? request.role() : Role.TENANT_USER;

        User user = User.builder()
                .tenantId(request.tenantId())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(role)
                .isActive(true)
                .failedAttempts(0)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getId());

        // Publish event to Kafka → Audit service will record this
        authEventProducer.publishUserRegisteredEvent(savedUser);

        return UserResponse.from(savedUser);
    }

    // --- Login ---

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        // Find user by email
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with email: " + request.email()));

        // Check if account is locked
        if (!user.isAccountNonLocked()) {
            throw new AccountLockedException(
                    "Account is locked until: " + user.getLockedUntil());
        }

        // Attempt authentication
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );
        } catch (BadCredentialsException e) {
            // Track failed attempts and lock if needed
            handleFailedLogin(user);
            throw new BadCredentialsException("Invalid credentials");
        }

        // Reset failed attempts on successful login
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = generateAndSaveRefreshToken(user);

        log.info("User logged in successfully: {}", user.getId());

        // Publish event to Kafka
        authEventProducer.publishUserLoggedInEvent(user);

        return AuthResponse.of(
                accessToken,
                refreshToken,
                jwtService.extractRemainingValidity(accessToken),
                user.getId(),
                user.getTenantId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }

    // --- Refresh Token ---

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        log.info("Refresh token request received");

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.refreshToken())
                .orElseThrow(() -> new InvalidTokenException(
                        "Refresh token not found"));

        // Check if revoked or expired
        if (refreshToken.isRevoked()) {
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        if (refreshToken.isExpired()) {
            throw new InvalidTokenException("Refresh token has expired");
        }

        User user = refreshToken.getUser();

        // Revoke old refresh token — rotate tokens for security
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        // Generate new tokens
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = generateAndSaveRefreshToken(user);

        log.info("Tokens refreshed for user: {}", user.getId());

        return AuthResponse.of(
                newAccessToken,
                newRefreshToken,
                jwtService.extractRemainingValidity(newAccessToken),
                user.getId(),
                user.getTenantId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }

    // --- Logout ---

    @Override
    @Transactional
    public void logout(String token) {
        log.info("Logout request received");

        // Blacklist access token in Redis
        tokenBlacklistService.blacklistToken(token);

        // Revoke all refresh tokens for this user
        String email = jwtService.extractEmail(token);
        userRepository.findByEmail(email).ifPresent(user -> {
            refreshTokenRepository.revokeAllUserTokens(user);
            authEventProducer.publishUserLoggedOutEvent(user);
            log.info("User logged out: {}", user.getId());
        });
    }

    // --- Validate Token ---

    @Override
    public boolean validateToken(String token) {
        try {
            // Check blacklist first
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                return false;
            }
            // Check expiry
            return !jwtService.isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // --- Private Helpers ---

    private String generateAndSaveRefreshToken(User user) {
        String tokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now()
                        .plusSeconds(refreshExpirationMs / 1000))
                .isRevoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }

    private void handleFailedLogin(User user) {
        int attempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            // Lock account for 30 minutes
            user.setLockedUntil(
                    LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
            log.warn("Account locked for user: {} until: {}",
                    user.getEmail(), user.getLockedUntil());

            // Publish lock event to Kafka
            authEventProducer.publishUserLockedEvent(user);
        }

        userRepository.save(user);
    }
}
