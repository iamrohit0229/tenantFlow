package com.tenantflow.tenant.exception;

import org.springframework.http.HttpStatus;

public class TenantAlreadyExistsException extends TenantFlowException {

    private static final String ERROR_CODE = "TENANT_ALREADY_EXISTS";

    public TenantAlreadyExistsException(String field, String value) {
        super(
                "Tenant already exists with " + field + ": " + value,
                HttpStatus.CONFLICT,
                ERROR_CODE
        );
    }
}
