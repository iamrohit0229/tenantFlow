package com.tenantflow.tenant.dto.request;

import com.tenantflow.tenant.entity.SubscriptionPlan;
import com.tenantflow.tenant.entity.TenantStatus;
import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record UpdateTenantRequest(

        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        TenantStatus status,

        SubscriptionPlan plan
) {}
