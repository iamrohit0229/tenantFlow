package com.tenantflow.tenant.service;

import com.tenantflow.tenant.model.Tenant;
import com.tenantflow.tenant.model.TenantStatus;
import com.tenantflow.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantServiceImpl implements TenantService {

    // Constructor Injection via @RequiredArgsConstructor
    // This is Module 1 DI concept applied here!
    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public Tenant createTenant(Tenant tenant) {
        log.info("Creating new tenant with email: {}",
                tenant.getEmail());

        // Check if email already exists
        if (tenantRepository.existsByEmail(tenant.getEmail())) {
            throw new RuntimeException(
                    "Tenant with email already exists: "
                            + tenant.getEmail()
            );
        }

        // Check if domain already exists
        if (tenantRepository.existsByDomain(tenant.getDomain())) {
            throw new RuntimeException(
                    "Tenant with domain already exists: "
                            + tenant.getDomain()
            );
        }

        // Set default status to ACTIVE
        tenant.setStatus(TenantStatus.ACTIVE);

        Tenant savedTenant = tenantRepository.save(tenant);
        log.info("Tenant created successfully with id: {}",
                savedTenant.getId());

        return savedTenant;
    }

    @Override
    public Tenant getTenantById(Long id) {
        log.info("Fetching tenant with id: {}", id);

        return tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Tenant not found with id: " + id
                ));
    }

    @Override
    public Tenant getTenantByDomain(String domain) {
        log.info("Fetching tenant with domain: {}", domain);

        return tenantRepository.findByDomain(domain)
                .orElseThrow(() -> new RuntimeException(
                        "Tenant not found with domain: " + domain
                ));
    }

    @Override
    public List<Tenant> getAllTenants() {
        log.info("Fetching all tenants");
        return tenantRepository.findAll();
    }

    @Override
    public List<Tenant> getTenantsByStatus(TenantStatus status) {
        log.info("Fetching tenants with status: {}", status);
        return tenantRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public Tenant updateTenant(Long id, Tenant tenant) {
        log.info("Updating tenant with id: {}", id);

        Tenant existingTenant = getTenantById(id);

        existingTenant.setName(tenant.getName());
        existingTenant.setEmail(tenant.getEmail());
        existingTenant.setDomain(tenant.getDomain());
        existingTenant.setStatus(tenant.getStatus());

        Tenant updatedTenant = tenantRepository
                .save(existingTenant);

        log.info("Tenant updated successfully with id: {}",
                updatedTenant.getId());

        return updatedTenant;
    }

    @Override
    @Transactional
    public void deleteTenant(Long id) {
        log.info("Deleting tenant with id: {}", id);

        Tenant tenant = getTenantById(id);
        tenantRepository.delete(tenant);

        log.info("Tenant deleted successfully with id: {}", id);
    }
}
