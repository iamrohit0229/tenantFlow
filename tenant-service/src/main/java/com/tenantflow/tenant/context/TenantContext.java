package com.tenantflow.tenant.context;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TenantContext {

    // ThreadLocal ensures each request thread
    // has its own isolated tenant identifier
    private static final ThreadLocal<String> CURRENT_TENANT =
            new ThreadLocal<>();

    public static void setTenantId(String tenantId) {
        log.debug("Setting tenant context to: {}", tenantId);
        CURRENT_TENANT.set(tenantId);
    }

    public static String getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        log.debug("Clearing tenant context");
        CURRENT_TENANT.remove();
    }
}
