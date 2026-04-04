package com.tenantflow.tenant.repository;

import com.tenantflow.tenant.model.Tenant;
import com.tenantflow.tenant.model.TenantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRepository
        extends JpaRepository<Tenant, Long> {

    // Spring Data JPA generates queries
    // automatically from method names!

    Optional<Tenant> findByEmail(String email);

    Optional<Tenant> findByDomain(String domain);

    List<Tenant> findByStatus(TenantStatus status);

    boolean existsByEmail(String email);

    boolean existsByDomain(String domain);
}
