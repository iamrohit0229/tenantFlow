package com.tenantflow.tenant.exception;

import org.springframework.http.HttpStatus;

public class TenantOperationException extends TenantFlowException {

    private static final String ERROR_CODE = "TENANT_OPERATION_FAILED";

    public TenantOperationException(String message) {
        super(
                message,
                HttpStatus.UNPROCESSABLE_ENTITY,
                ERROR_CODE
        );
    }

    public TenantOperationException(String message, HttpStatus status) {
        super(
                message,
                status,
                ERROR_CODE
        );
    }
}
