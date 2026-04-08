package com.tenantflow.identity.repository;

import com.tenantflow.identity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Find user by email for login
    Optional<User> findByEmail(String email);

    // Find user by email within a specific tenant
    Optional<User> findByEmailAndTenantId(String email, UUID tenantId);

    // Check if email already exists in a tenant
    boolean existsByEmailAndTenantId(String email, UUID tenantId);

    // Get all users of a tenant
    List<User> findAllByTenantId(UUID tenantId);

    // Get active users of a tenant
    List<User> findAllByTenantIdAndIsActive(UUID tenantId, boolean isActive);
}
