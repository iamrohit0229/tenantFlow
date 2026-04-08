-- Users table for identity-service
-- Every user belongs to a tenant

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'TENANT_USER',
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_login_at   TIMESTAMP,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked_until    TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Email must be unique per tenant
    -- Two different tenants CAN have same email
    CONSTRAINT uk_users_email_tenant UNIQUE (email, tenant_id)
);

-- Index for fast login lookup
CREATE INDEX idx_users_email ON users(email);

-- Index for tenant based queries
CREATE INDEX idx_users_tenant_id ON users(tenant_id);
