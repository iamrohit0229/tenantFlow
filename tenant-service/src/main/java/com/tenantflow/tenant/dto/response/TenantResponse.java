package com.tenantflow.tenant.dto.response;

import com.tenantflow.tenant.entity.SubscriptionPlan;
import com.tenantflow.tenant.entity.TenantStatus;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record TenantResponse(

        UUID id,
        String name,
        String subdomain,
        TenantStatus status,
        SubscriptionPlan plan,
        Long quotaLimit,
        Long currentUsageCount,
        Double usagePercentage,
        LocalDate lastResetDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}
