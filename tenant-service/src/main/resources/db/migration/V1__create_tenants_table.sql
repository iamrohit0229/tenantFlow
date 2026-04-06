-- ============================================================
-- V1__create_tenants_table.sql
-- TenantFlow SaaS Platform
-- Core tenant registry table
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE tenants (

    id                  UUID            PRIMARY KEY DEFAULT uuid_generate_v4(),
    name                VARCHAR(100)    NOT NULL,
    subdomain           VARCHAR(50)     NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    plan                VARCHAR(20)     NOT NULL DEFAULT 'FREE',
    quota_limit         BIGINT          NOT NULL DEFAULT 1000,
    current_usage_count BIGINT          NOT NULL DEFAULT 0,
    last_reset_date     DATE            NOT NULL DEFAULT CURRENT_DATE,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    version             BIGINT          NOT NULL DEFAULT 0,
    deleted_at          TIMESTAMP,

    CONSTRAINT uq_tenants_name
        UNIQUE (name),
    CONSTRAINT uq_tenants_subdomain
        UNIQUE (subdomain),
    CONSTRAINT chk_tenants_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED')),
    CONSTRAINT chk_tenants_plan
        CHECK (plan IN ('FREE', 'PRO', 'ENTERPRISE')),
    CONSTRAINT chk_tenants_quota_limit
        CHECK (quota_limit >= 0),
    CONSTRAINT chk_tenants_usage_count
        CHECK (current_usage_count >= 0)
);

CREATE INDEX idx_tenants_status
    ON tenants(status)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_tenants_plan
    ON tenants(plan)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_tenants_subdomain
    ON tenants(subdomain)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_tenants_deleted_at
    ON tenants(deleted_at);

CREATE INDEX idx_tenants_last_reset_date
    ON tenants(last_reset_date)
    WHERE deleted_at IS NULL;

COMMENT ON TABLE tenants IS
    'Core tenant registry for TenantFlow SaaS platform';
COMMENT ON COLUMN tenants.subdomain IS
    'Unique subdomain identifier e.g. acme-corp';
COMMENT ON COLUMN tenants.status IS
    'Tenant lifecycle: ACTIVE, INACTIVE, SUSPENDED, DELETED';
COMMENT ON COLUMN tenants.plan IS
    'Subscription plan: FREE=1000/day PRO=50000/day ENTERPRISE=unlimited';
COMMENT ON COLUMN tenants.quota_limit IS
    'Max API requests allowed per day based on plan';
COMMENT ON COLUMN tenants.current_usage_count IS
    'Current API request count for today reset daily';
COMMENT ON COLUMN tenants.last_reset_date IS
    'Date when usage count was last reset to zero';
COMMENT ON COLUMN tenants.version IS
    'Optimistic locking version incremented on every update';
COMMENT ON COLUMN tenants.created_by IS
    'Username or system that created this tenant record';
COMMENT ON COLUMN tenants.updated_by IS
    'Username or system that last updated this tenant record';
COMMENT ON COLUMN tenants.deleted_at IS
    'Soft delete timestamp NULL means record is active';
