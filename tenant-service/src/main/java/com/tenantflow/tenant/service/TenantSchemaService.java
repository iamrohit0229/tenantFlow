package com.tenantflow.tenant.service;

public interface TenantSchemaService {

    /**
     * Creates a dedicated PostgreSQL schema for the tenant.
     * Schema name format: tenant_{subdomain}
     * Called immediately after tenant is persisted.
     */
    void createSchemaForTenant(String subdomain);

    /**
     * Drops the schema for a tenant.
     * Only called on hard delete — never on soft delete!
     */
    void dropSchemaForTenant(String subdomain);

    /**
     * Checks if schema already exists for a given subdomain.
     */
    boolean schemaExists(String subdomain);
}
