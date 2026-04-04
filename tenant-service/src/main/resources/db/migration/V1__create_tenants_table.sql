CREATE TABLE IF NOT EXISTS tenants
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255)        NOT NULL,
    email      VARCHAR(255) UNIQUE NOT NULL,
    domain     VARCHAR(255) UNIQUE NOT NULL,
    status     VARCHAR(50)         NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Insert some test data
INSERT INTO tenants (name, email, domain, status, created_at, updated_at)
VALUES ('TechCorp',
        'admin@techcorp.com',
        'techcorp.com',
        'ACTIVE',
        NOW(),
        NOW()),
       ('StartupXYZ',
        'admin@startupxyz.com',
        'startupxyz.com',
        'ACTIVE',
        NOW(),
        NOW());
