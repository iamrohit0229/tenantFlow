package com.tenantflow.tenant.mapper;

import com.tenantflow.tenant.dto.request.TenantRequest;
import com.tenantflow.tenant.dto.request.UpdateTenantRequest;
import com.tenantflow.tenant.dto.response.TenantResponse;
import com.tenantflow.tenant.entity.Tenant;
import com.tenantflow.tenant.entity.SubscriptionPlan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TenantMapper {

    // ─────────────────────────────────────────
    // Request → Entity
    // ─────────────────────────────────────────

    public Tenant toEntity(TenantRequest request) {
        log.debug("Mapping TenantRequest to Tenant entity for subdomain: {}",
                request.subdomain());

        SubscriptionPlan plan = request.plan();

        return Tenant.builder()
                .name(request.name().trim())
                .subdomain(request.subdomain().toLowerCase().trim())
                .plan(plan)
                .quotaLimit(plan.getQuotaLimit())
                .build();
    }

    // ─────────────────────────────────────────
    // Entity → Response
    // ─────────────────────────────────────────

    public TenantResponse toResponse(Tenant tenant) {
        log.debug("Mapping Tenant entity to TenantResponse for id: {}",
                tenant.getId());

        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .subdomain(tenant.getSubdomain())
                .status(tenant.getStatus())
                .plan(tenant.getPlan())
                .quotaLimit(tenant.getQuotaLimit())
                .currentUsageCount(tenant.getCurrentUsageCount())
                .usagePercentage(calculateUsagePercentage(tenant))
                .lastResetDate(tenant.getLastResetDate())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .createdBy(tenant.getCreatedBy())
                .updatedBy(tenant.getUpdatedBy())
                .build();
    }

    // ─────────────────────────────────────────
    // Apply Update Request → Existing Entity
    // ─────────────────────────────────────────

    public void updateEntity(UpdateTenantRequest request, Tenant tenant) {
        log.debug("Applying UpdateTenantRequest to Tenant entity for id: {}",
                tenant.getId());

        if (request.name() != null && !request.name().isBlank()) {
            tenant.setName(request.name().trim());
        }

        if (request.status() != null) {
            validateStatusTransition(tenant, request);
            tenant.setStatus(request.status());
        }

        if (request.plan() != null) {
            tenant.upgradePlan(request.plan());
        }
    }

    // ─────────────────────────────────────────
    // Private Helper Methods
    // ─────────────────────────────────────────

    private double calculateUsagePercentage(Tenant tenant) {
        if (tenant.getPlan().isUnlimited()) {
            return 0.0;
        }

        if (tenant.getQuotaLimit() == 0) {
            return 0.0;
        }

        double percentage = (double) tenant.getCurrentUsageCount()
                / tenant.getQuotaLimit()
                * 100.0;

        // Cap at 100% even if somehow exceeded
        return Math.min(percentage, 100.0);
    }

    private void validateStatusTransition(Tenant tenant, UpdateTenantRequest request) {
        boolean isValid = switch (request.status()) {
            case ACTIVE     -> tenant.getStatus().canBeActivated();
            case SUSPENDED  -> tenant.getStatus().canBeSuspended();
            case INACTIVE   -> tenant.getStatus() != tenant.getStatus().DELETED;
            case DELETED    -> !tenant.getStatus().isSoftDeleted();
        };

        if (!isValid) {
            throw new IllegalStateException(
                    "Cannot transition tenant status from %s to %s"
                            .formatted(tenant.getStatus(), request.status())
            );
        }
    }
}
