package com.tenantflow.tenant.service;

import com.tenantflow.tenant.dto.request.TenantRequest;
import com.tenantflow.tenant.dto.request.UpdateTenantRequest;
import com.tenantflow.tenant.dto.response.PageResponse;
import com.tenantflow.tenant.dto.response.TenantResponse;
import com.tenantflow.tenant.dto.response.TenantStatsResponse;
import com.tenantflow.tenant.entity.SubscriptionPlan;
import com.tenantflow.tenant.entity.TenantStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TenantService {

    // ─────────────────────────────────────────
    // CRUD Operations
    // ─────────────────────────────────────────

    TenantResponse createTenant(TenantRequest request);

    TenantResponse getTenantById(UUID id);

    TenantResponse getTenantBySubdomain(String subdomain);

    TenantResponse updateTenant(UUID id, UpdateTenantRequest request);

    void deleteTenant(UUID id);

    // ─────────────────────────────────────────
    // List + Filter Operations
    // ─────────────────────────────────────────

    PageResponse<TenantResponse> getAllTenants(Pageable pageable);

    PageResponse<TenantResponse> getTenantsByStatus(
            TenantStatus status,
            Pageable pageable
    );

    PageResponse<TenantResponse> getTenantsByPlan(
            SubscriptionPlan plan,
            Pageable pageable
    );

    // ─────────────────────────────────────────
    // Business Operations
    // ─────────────────────────────────────────

    TenantResponse suspendTenant(UUID id);

    TenantResponse activateTenant(UUID id);

    TenantResponse upgradePlan(UUID id, SubscriptionPlan newPlan);

    // ─────────────────────────────────────────
    // Stats + Dashboard
    // ─────────────────────────────────────────

    TenantStatsResponse getTenantStats();
}
