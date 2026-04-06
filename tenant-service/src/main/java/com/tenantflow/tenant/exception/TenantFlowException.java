package com.tenantflow.tenant.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class TenantFlowException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    protected TenantFlowException(
            String message,
            HttpStatus httpStatus,
            String errorCode
    ) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }
}
