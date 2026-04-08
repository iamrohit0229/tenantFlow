package com.tenantflow.tenant.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class TenantNotFoundException extends TenantFlowException {

    private static final String ERROR_CODE = "TENANT_NOT_FOUND";

    public TenantNotFoundException(UUID id) {
        super(
                "Tenant not found with id: " + id,
                HttpStatus.NOT_FOUND,
                ERROR_CODE
        );
    }

    public TenantNotFoundException(String subdomain) {
        super(
                "Tenant not found with subdomain: " + subdomain,
                HttpStatus.NOT_FOUND,
                ERROR_CODE
        );
    }
}
