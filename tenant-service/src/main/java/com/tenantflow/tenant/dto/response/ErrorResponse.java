package com.tenantflow.tenant.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(

        int status,
        String errorCode,
        String message,
        String path,
        LocalDateTime timestamp,
        Map<String, String> validationErrors
) {
    // Factory method for simple errors
    public static ErrorResponse of(
            int status,
            String errorCode,
            String message,
            String path
    ) {
        return ErrorResponse.builder()
                .status(status)
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Factory method for validation errors
    public static ErrorResponse ofValidation(
            String path,
            Map<String, String> validationErrors
    ) {
        return ErrorResponse.builder()
                .status(400)
                .errorCode("VALIDATION_FAILED")
                .message("Request validation failed")
                .path(path)
                .timestamp(LocalDateTime.now())
                .validationErrors(validationErrors)
                .build();
    }
}
