package com.tenantflow.identity.context;

public class TenantContext {

    // ThreadLocal stores tenantId per request thread
    // Each request thread has its own isolated copy
    private static final ThreadLocal<String> CURRENT_TENANT =
            new ThreadLocal<>();

    public static void setTenantId(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static String getTenantId() {
        return CURRENT_TENANT.get();
    }

    // MUST call this after every request
    // Otherwise memory leak in thread pool!
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
