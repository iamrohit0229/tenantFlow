package com.tenantflow.tenant.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TenantStatus {

    ACTIVE("Tenant is active and can make API calls"),
    INACTIVE("Tenant is inactive - access suspended temporarily"),
    SUSPENDED("Tenant is suspended due to policy violation or non-payment"),
    DELETED("Tenant is soft deleted - data retained for audit");

    private final String description;

    public boolean isAccessAllowed() {
        return this == ACTIVE;
    }

    public boolean isSoftDeleted() {
        return this == DELETED;
    }

    public boolean canBeActivated() {
        return this == INACTIVE || this == SUSPENDED;
    }

    public boolean canBeSuspended() {
        return this == ACTIVE || this == INACTIVE;
    }
}
