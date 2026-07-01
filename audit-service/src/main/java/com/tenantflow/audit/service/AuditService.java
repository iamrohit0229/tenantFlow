package com.tenantflow.audit.service;

import com.tenantflow.audit.dto.response.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface AuditService {

    // Get all logs for a tenant — tenant isolation enforced!
    Page<AuditLogResponse> getLogsByTenant(String tenantId, Pageable pageable);

    // Filter by event type
    Page<AuditLogResponse> getLogsByTenantAndEventType(
            String tenantId, String eventType, Pageable pageable);

    // Filter by event source
    Page<AuditLogResponse> getLogsByTenantAndSource(
            String tenantId, String source, Pageable pageable);

    // Filter by userId
    Page<AuditLogResponse> getLogsByTenantAndUser(
            String tenantId, String userId, Pageable pageable);

    // Date range filter
    Page<AuditLogResponse> getLogsByTenantAndDateRange(
            String tenantId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable);

    // Count total logs for a tenant
    long countLogsByTenant(String tenantId);
}
