package com.tenantflow.tenant.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubscriptionPlan {

    FREE(1_000L, "Free Plan - 1,000 API calls/day"),
    PRO(50_000L, "Pro Plan - 50,000 API calls/day"),
    ENTERPRISE(Long.MAX_VALUE, "Enterprise Plan - Unlimited API calls/day");

    private final long quotaLimit;
    private final String description;

    public boolean isUnlimited() {
        return this == ENTERPRISE;
    }

    public boolean hasQuotaExceeded(long currentUsage) {
        if (isUnlimited()) {
            return false;
        }
        return currentUsage >= quotaLimit;
    }
}
