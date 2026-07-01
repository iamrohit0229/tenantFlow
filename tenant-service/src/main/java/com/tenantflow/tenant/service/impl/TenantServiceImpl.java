// tenant-service/src/main/java/com/tenantflow/tenant/service/impl/TenantServiceImpl.java

package com.tenantflow.tenant.service.impl;

import com.tenantflow.tenant.dto.request.TenantRequest;
import com.tenantflow.tenant.dto.request.UpdateTenantRequest;
import com.tenantflow.tenant.dto.response.PageResponse;
import com.tenantflow.tenant.dto.response.TenantResponse;
import com.tenantflow.tenant.dto.response.TenantStatsResponse;
import com.tenantflow.tenant.entity.SubscriptionPlan;
import com.tenantflow.tenant.entity.Tenant;
import com.tenantflow.tenant.entity.TenantStatus;
import com.tenantflow.tenant.exception.TenantAlreadyExistsException;
import com.tenantflow.tenant.exception.TenantNotFoundException;
import com.tenantflow.tenant.exception.TenantOperationException;
import com.tenantflow.tenant.kafka.TenantEventProducer;
import com.tenantflow.tenant.kafka.event.TenantEvent;
import com.tenantflow.tenant.mapper.TenantMapper;
import com.tenantflow.tenant.repository.TenantRepository;
import com.tenantflow.tenant.service.TenantSchemaService;
import com.tenantflow.tenant.service.TenantService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;
    private final TenantEventProducer tenantEventProducer;
    private final TenantSchemaService tenantSchemaService;

    @PersistenceContext
    private EntityManager entityManager;

    // ─────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────

    @Override
    @Transactional
    public TenantResponse createTenant(TenantRequest request) {
        log.info("Creating tenant with name: {} and subdomain: {}",
                request.name(), request.subdomain());

        // Check duplicates
        if (tenantRepository.existsByNameAndDeletedAtIsNull(request.name())) {
            throw new TenantAlreadyExistsException("name", request.name());
        }
        if (tenantRepository.existsBySubdomainAndDeletedAtIsNull(request.subdomain())) {
            throw new TenantAlreadyExistsException("subdomain", request.subdomain());
        }

        // Map and save
        Tenant tenant = tenantMapper.toEntity(request);
        Tenant savedTenant = tenantRepository.save(tenant);

        entityManager.flush();
        entityManager.refresh(savedTenant);

        // Create dedicated PostgreSQL schema for this tenant
        // This is the schema-per-tenant isolation pattern!
        tenantSchemaService.createSchemaForTenant(savedTenant.getSubdomain());

        // Publish event to Kafka — audit-service will consume this
        tenantEventProducer.publishTenantCreated(
                TenantEvent.created(
                        savedTenant.getId().toString(),
                        savedTenant.getName(),
                        savedTenant.getSubdomain(),
                        savedTenant.getPlan().name(),
                        savedTenant.getStatus().name()
                )
        );

        log.info("Tenant created successfully with id: {} and schema: tenant_{}",
                savedTenant.getId(), savedTenant.getSubdomain());

        return tenantMapper.toResponse(savedTenant);
    }

    // ─────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────

    @Override
    public TenantResponse getTenantById(UUID id) {
        log.debug("Fetching tenant by id: {}", id);
        return tenantMapper.toResponse(findTenantByIdOrThrow(id));
    }

    @Override
    public TenantResponse getTenantBySubdomain(String subdomain) {
        log.debug("Fetching tenant by subdomain: {}", subdomain);

        Tenant tenant = tenantRepository
                .findBySubdomainAndDeletedAtIsNull(subdomain)
                .orElseThrow(() -> new TenantNotFoundException(subdomain));

        return tenantMapper.toResponse(tenant);
    }

    @Override
    public PageResponse<TenantResponse> getAllTenants(Pageable pageable) {
        log.debug("Fetching all tenants - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<TenantResponse> page = tenantRepository
                .findAllByDeletedAtIsNull(pageable)
                .map(tenantMapper::toResponse);

        return PageResponse.from(page);
    }

    @Override
    public PageResponse<TenantResponse> getTenantsByStatus(
            TenantStatus status, Pageable pageable) {
        log.debug("Fetching tenants by status: {}", status);

        Page<TenantResponse> page = tenantRepository
                .findAllByStatusAndDeletedAtIsNull(status, pageable)
                .map(tenantMapper::toResponse);

        return PageResponse.from(page);
    }

    @Override
    public PageResponse<TenantResponse> getTenantsByPlan(
            SubscriptionPlan plan, Pageable pageable) {
        log.debug("Fetching tenants by plan: {}", plan);

        Page<TenantResponse> page = tenantRepository
                .findAllByPlanAndDeletedAtIsNull(plan, pageable)
                .map(tenantMapper::toResponse);

        return PageResponse.from(page);
    }

    // ─────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────

    @Override
    @Transactional
    public TenantResponse updateTenant(UUID id, UpdateTenantRequest request) {
        log.info("Updating tenant with id: {}", id);

        Tenant tenant = findTenantByIdOrThrow(id);

        // Check name conflict with OTHER tenants
        if (request.name() != null &&
                tenantRepository.existsByNameAndIdNotAndDeletedAtIsNull(
                        request.name(), id)) {
            throw new TenantAlreadyExistsException("name", request.name());
        }

        tenantMapper.updateEntity(request, tenant);
        Tenant updatedTenant = tenantRepository.save(tenant);

        // Publish update event — audit trail records every change
        tenantEventProducer.publishTenantUpdated(
                TenantEvent.updated(
                        updatedTenant.getId().toString(),
                        updatedTenant.getName(),
                        updatedTenant.getSubdomain(),
                        updatedTenant.getPlan().name(),
                        updatedTenant.getStatus().name()
                )
        );

        log.info("Tenant updated successfully with id: {}", id);
        return tenantMapper.toResponse(updatedTenant);
    }

    // ─────────────────────────────────────────
    // DELETE (Soft)
    // ─────────────────────────────────────────

    @Override
    @Transactional
    public void deleteTenant(UUID id) {
        log.info("Soft deleting tenant with id: {}", id);

        Tenant tenant = findTenantByIdOrThrow(id);

        if (tenant.isDeleted()) {
            throw new TenantOperationException(
                    "Tenant with id: " + id + " is already deleted"
            );
        }

        tenant.softDelete();
        tenantRepository.save(tenant);

        // Publish deleted event
        // Note: soft delete — schema is preserved
        // Data still exists but tenant is deactivated
        tenantEventProducer.publishTenantDeleted(
                TenantEvent.deleted(
                        tenant.getId().toString(),
                        tenant.getName(),
                        tenant.getSubdomain(),
                        tenant.getPlan().name()
                )
        );

        log.info("Tenant soft deleted successfully with id: {}", id);
    }

    // ─────────────────────────────────────────
    // BUSINESS OPERATIONS
    // ─────────────────────────────────────────

    @Override
    @Transactional
    public TenantResponse suspendTenant(UUID id) {
        log.info("Suspending tenant with id: {}", id);

        Tenant tenant = findTenantByIdOrThrow(id);

        if (!tenant.getStatus().canBeSuspended()) {
            throw new TenantOperationException(
                    "Tenant with status: " + tenant.getStatus()
                            + " cannot be suspended"
            );
        }

        tenant.setStatus(TenantStatus.SUSPENDED);
        Tenant savedTenant = tenantRepository.save(tenant);

        // Publish suspended event — critical for billing + audit
        tenantEventProducer.publishTenantSuspended(
                TenantEvent.suspended(
                        savedTenant.getId().toString(),
                        savedTenant.getName(),
                        savedTenant.getSubdomain(),
                        savedTenant.getPlan().name()
                )
        );

        log.info("Tenant suspended successfully with id: {}", id);
        return tenantMapper.toResponse(savedTenant);
    }

    @Override
    @Transactional
    public TenantResponse activateTenant(UUID id) {
        log.info("Activating tenant with id: {}", id);

        Tenant tenant = findTenantByIdOrThrow(id);

        if (!tenant.getStatus().canBeActivated()) {
            throw new TenantOperationException(
                    "Tenant with status: " + tenant.getStatus()
                            + " cannot be activated"
            );
        }

        tenant.setStatus(TenantStatus.ACTIVE);
        Tenant savedTenant = tenantRepository.save(tenant);

        // Publish updated event on activation
        tenantEventProducer.publishTenantUpdated(
                TenantEvent.updated(
                        savedTenant.getId().toString(),
                        savedTenant.getName(),
                        savedTenant.getSubdomain(),
                        savedTenant.getPlan().name(),
                        savedTenant.getStatus().name()
                )
        );

        log.info("Tenant activated successfully with id: {}", id);
        return tenantMapper.toResponse(savedTenant);
    }

    @Override
    @Transactional
    public TenantResponse upgradePlan(UUID id, SubscriptionPlan newPlan) {
        log.info("Upgrading plan for tenant id: {} to plan: {}", id, newPlan);

        Tenant tenant = findTenantByIdOrThrow(id);

        if (tenant.getPlan() == newPlan) {
            throw new TenantOperationException(
                    "Tenant is already on plan: " + newPlan
            );
        }

        tenant.upgradePlan(newPlan);
        Tenant savedTenant = tenantRepository.save(tenant);

        // Publish plan upgrade event — billing-service tracks this
        tenantEventProducer.publishTenantUpdated(
                TenantEvent.updated(
                        savedTenant.getId().toString(),
                        savedTenant.getName(),
                        savedTenant.getSubdomain(),
                        savedTenant.getPlan().name(),
                        savedTenant.getStatus().name()
                )
        );

        log.info("Plan upgraded successfully for tenant id: {} to: {}",
                id, newPlan);
        return tenantMapper.toResponse(savedTenant);
    }

    // ─────────────────────────────────────────
    // STATS
    // ─────────────────────────────────────────

    @Override
    public TenantStatsResponse getTenantStats() {
        log.debug("Fetching tenant statistics");

        return TenantStatsResponse.builder()
                .totalTenants(
                        tenantRepository.countByDeletedAtIsNull())
                .activeTenants(
                        tenantRepository.countByStatusAndDeletedAtIsNull(TenantStatus.ACTIVE))
                .inactiveTenants(
                        tenantRepository.countByStatusAndDeletedAtIsNull(TenantStatus.INACTIVE))
                .suspendedTenants(
                        tenantRepository.countByStatusAndDeletedAtIsNull(TenantStatus.SUSPENDED))
                .freePlanTenants(
                        tenantRepository.countByPlanAndDeletedAtIsNull(SubscriptionPlan.FREE))
                .proPlanTenants(
                        tenantRepository.countByPlanAndDeletedAtIsNull(SubscriptionPlan.PRO))
                .enterprisePlanTenants(
                        tenantRepository.countByPlanAndDeletedAtIsNull(SubscriptionPlan.ENTERPRISE))
                .tenantsExceededQuota(
                        tenantRepository.findTenantsWithExceededQuota().size())
                .tenantsApproachingQuota(
                        tenantRepository.findTenantsApproachingQuota().size())
                .build();
    }

    // ─────────────────────────────────────────
    // Private Helpers
    // ─────────────────────────────────────────

    private Tenant findTenantByIdOrThrow(UUID id) {
        return tenantRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new TenantNotFoundException(id));
    }
}
