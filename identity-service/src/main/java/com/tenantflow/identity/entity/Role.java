package com.tenantflow.identity.entity;

public enum Role {

    SUPER_ADMIN,    // Platform level — can manage all tenants
    TENANT_ADMIN,   // Admin of their own tenant only
    TENANT_USER     // Regular user of a tenant
}
