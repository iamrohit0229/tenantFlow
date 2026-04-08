package com.tenantflow.identity.service.impl;

import com.tenantflow.identity.dto.request.RegisterRequest;
import com.tenantflow.identity.dto.response.UserResponse;
import com.tenantflow.identity.entity.User;
import com.tenantflow.identity.exception.UserNotFoundException;
import com.tenantflow.identity.repository.UserRepository;
import com.tenantflow.identity.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // --- Get All Users By Tenant ---

    @Override
    public List<UserResponse> getAllUsersByTenant(UUID tenantId) {
        log.info("Fetching all users for tenant: {}", tenantId);

        return userRepository.findAllByTenantId(tenantId)
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    // --- Get User By Id ---

    @Override
    public UserResponse getUserById(UUID userId) {
        log.info("Fetching user by id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + userId));

        return UserResponse.from(user);
    }

    // --- Update User ---

    @Override
    @Transactional
    public UserResponse updateUser(UUID userId, RegisterRequest request) {
        log.info("Updating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + userId));

        // Update only provided fields
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", userId);

        return UserResponse.from(updatedUser);
    }

    // --- Deactivate User ---

    @Override
    @Transactional
    public void deactivateUser(UUID userId) {
        log.info("Deactivating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + userId));

        // Soft delete — never hard delete users
        user.setActive(false);
        userRepository.save(user);

        log.info("User deactivated: {}", userId);
    }
}
