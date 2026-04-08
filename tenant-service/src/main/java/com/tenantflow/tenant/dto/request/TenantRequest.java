package com.tenantflow.tenant.dto.request;

import com.tenantflow.tenant.entity.SubscriptionPlan;
import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record TenantRequest(

        @NotBlank(message = "Tenant name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Subdomain is required")
        @Size(min = 2, max = 50, message = "Subdomain must be between 2 and 50 characters")
        @Pattern(
                regexp = "^[a-z0-9-]+$",
                message = "Subdomain can only contain lowercase letters, numbers and hyphens"
        )
        String subdomain,

        @NotNull(message = "Subscription plan is required")
        SubscriptionPlan plan
) {}
