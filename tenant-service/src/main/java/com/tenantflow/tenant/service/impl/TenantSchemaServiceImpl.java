package com.tenantflow.tenant.service.impl;

import com.tenantflow.tenant.service.TenantSchemaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantSchemaServiceImpl implements TenantSchemaService {

    private final JdbcTemplate jdbcTemplate;

    // Schema name prefix — all tenant schemas follow this pattern
    private static final String SCHEMA_PREFIX = "tenant_";

    @Override
    public void createSchemaForTenant(String subdomain) {
        String schemaName = buildSchemaName(subdomain);

        log.info("Creating PostgreSQL schema: {}", schemaName);

        // Validate schema name to prevent SQL injection
        // subdomain is already validated by @Pattern in TenantRequest
        // but we double-check here — defense in depth!
        validateSchemaName(schemaName);

        if (schemaExists(subdomain)) {
            log.warn("Schema [{}] already exists — skipping creation", schemaName);
            return;
        }

        // Raw SQL — JdbcTemplate because schema DDL
        // cannot be parameterized in PostgreSQL
        String sql = "CREATE SCHEMA IF NOT EXISTS " + schemaName;
        jdbcTemplate.execute(sql);

        log.info("Schema [{}] created successfully", schemaName);
    }

    @Override
    public void dropSchemaForTenant(String subdomain) {
        String schemaName = buildSchemaName(subdomain);

        log.warn("Dropping PostgreSQL schema: {} — THIS IS IRREVERSIBLE!", schemaName);

        validateSchemaName(schemaName);

        // CASCADE drops all tables inside the schema too
        String sql = "DROP SCHEMA IF EXISTS " + schemaName + " CASCADE";
        jdbcTemplate.execute(sql);

        log.warn("Schema [{}] dropped successfully", schemaName);
    }

    @Override
    public boolean schemaExists(String subdomain) {
        String schemaName = buildSchemaName(subdomain);

        // Query PostgreSQL system catalog to check schema existence
        String sql = """
                SELECT COUNT(*) FROM information_schema.schemata
                WHERE schema_name = ?
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, schemaName);
        return count != null && count > 0;
    }

    // ─────────────────────────────────────────
    // Private Helpers
    // ─────────────────────────────────────────

    private String buildSchemaName(String subdomain) {
        // Always lowercase — PostgreSQL schema names are case-sensitive!
        return SCHEMA_PREFIX + subdomain.toLowerCase();
    }

    private void validateSchemaName(String schemaName) {
        // Only allow alphanumeric + underscore — no SQL injection possible
        if (!schemaName.matches("^[a-z0-9_]+$")) {
            throw new IllegalArgumentException(
                    "Invalid schema name: [" + schemaName + "]. " +
                            "Only lowercase letters, digits, and underscores allowed."
            );
        }
    }
}
