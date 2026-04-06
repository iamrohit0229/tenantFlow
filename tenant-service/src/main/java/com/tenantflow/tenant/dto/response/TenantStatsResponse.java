// src/main/java/com/tenantflow/tenant/dto/response/TenantStatsResponse.java

package com.tenantflow.tenant.dto.response;

import lombok.Builder;

@Builder
public record TenantStatsResponse(

        long totalTenants,
        long activeTenants,
        long inactiveTenants,
        long suspendedTenants,
        long freePlanTenants,
        long proPlanTenants,
        long enterprisePlanTenants,
        long tenantsExceededQuota,
        long tenantsApproachingQuota
) {}
