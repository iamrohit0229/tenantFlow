-- billing-service/src/main/resources/db/migration/V1__create_billing_tables.sql

-- ============================================================
-- V1__create_billing_tables.sql
-- TenantFlow SaaS Platform
-- Billing: usage records + invoices + subscriptions
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ─────────────────────────────────────────────────────────────
-- USAGE RECORDS
-- Every API call = one record (event sourcing)
-- Billing is calculated FROM this table
-- ─────────────────────────────────────────────────────────────
CREATE TABLE usage_records (
    id                  UUID            PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id           UUID            NOT NULL,
    event_id            VARCHAR(255)    NOT NULL,   -- Kafka event ID for idempotency
    endpoint            VARCHAR(500)    NOT NULL,   -- which API was called
    http_method         VARCHAR(10)     NOT NULL,   -- GET POST PUT DELETE
    status_code         INT             NOT NULL,   -- response status
    response_time_ms    BIGINT,                     -- latency in ms
    usage_date          DATE            NOT NULL DEFAULT CURRENT_DATE,
    recorded_at         TIMESTAMP       NOT NULL DEFAULT NOW(),

    -- Idempotency: same Kafka event processed only once
    CONSTRAINT uq_usage_event_id UNIQUE (event_id)
);

CREATE INDEX idx_usage_tenant_id
    ON usage_records(tenant_id);

CREATE INDEX idx_usage_tenant_date
    ON usage_records(tenant_id, usage_date);

CREATE INDEX idx_usage_recorded_at
    ON usage_records(recorded_at);

COMMENT ON TABLE usage_records IS
    'Immutable API usage log — one row per API call — event sourcing basis for billing';
COMMENT ON COLUMN usage_records.event_id IS
    'Kafka event ID used for idempotent consumer — prevents duplicate processing';

-- ─────────────────────────────────────────────────────────────
-- SUBSCRIPTIONS
-- Current plan per tenant
-- ─────────────────────────────────────────────────────────────
CREATE TABLE subscriptions (
    id                  UUID            PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id           UUID            NOT NULL UNIQUE,
    plan                VARCHAR(20)     NOT NULL DEFAULT 'FREE',
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    quota_limit         BIGINT          NOT NULL DEFAULT 1000,
    billing_cycle_start DATE            NOT NULL DEFAULT CURRENT_DATE,
    billing_cycle_end   DATE            NOT NULL DEFAULT (CURRENT_DATE + INTERVAL '30 days'),
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_subscription_plan
        CHECK (plan IN ('FREE', 'PRO', 'ENTERPRISE')),
    CONSTRAINT chk_subscription_status
        CHECK (status IN ('ACTIVE', 'CANCELLED', 'SUSPENDED', 'PAST_DUE'))
);

CREATE INDEX idx_subscriptions_tenant_id
    ON subscriptions(tenant_id);

CREATE INDEX idx_subscriptions_status
    ON subscriptions(status);

COMMENT ON TABLE subscriptions IS
    'Current subscription plan per tenant with billing cycle info';

-- ─────────────────────────────────────────────────────────────
-- INVOICES
-- Generated at end of billing cycle
-- ─────────────────────────────────────────────────────────────
CREATE TABLE invoices (
    id                  UUID            PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id           UUID            NOT NULL,
    subscription_id     UUID            NOT NULL REFERENCES subscriptions(id),
    invoice_number      VARCHAR(50)     NOT NULL UNIQUE,
    status              VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',
    plan                VARCHAR(20)     NOT NULL,
    total_api_calls     BIGINT          NOT NULL DEFAULT 0,
    amount_cents        BIGINT          NOT NULL DEFAULT 0,  -- store in cents avoid float
    currency            VARCHAR(3)      NOT NULL DEFAULT 'USD',
    billing_period_start DATE           NOT NULL,
    billing_period_end   DATE           NOT NULL,
    issued_at           TIMESTAMP,
    due_at              TIMESTAMP,
    paid_at             TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_invoice_status
        CHECK (status IN ('DRAFT', 'ISSUED', 'PAID', 'OVERDUE', 'CANCELLED')),
    CONSTRAINT chk_invoice_plan
        CHECK (plan IN ('FREE', 'PRO', 'ENTERPRISE'))
);

CREATE INDEX idx_invoices_tenant_id
    ON invoices(tenant_id);

CREATE INDEX idx_invoices_status
    ON invoices(status);

CREATE INDEX idx_invoices_tenant_status
    ON invoices(tenant_id, status);

COMMENT ON TABLE invoices IS
    'Generated invoices per tenant per billing cycle';
COMMENT ON COLUMN invoices.amount_cents IS
    'Amount stored in cents to avoid floating point issues e.g. 1000 = $10.00';
