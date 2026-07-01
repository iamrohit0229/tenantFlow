package com.tenantflow.audit.controller;

import com.tenantflow.audit.dto.response.AuditLogResponse;
import com.tenantflow.audit.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Query audit logs per tenant")
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/tenants/{tenantId}/logs")
    @Operation(summary = "Get all audit logs for a tenant")
    public ResponseEntity<Page<AuditLogResponse>> getLogsByTenant(
            @PathVariable String tenantId,
            @PageableDefault(size = 20, sort = "occurredAt") Pageable pageable) {

        log.info("GET /api/v1/audit/tenants/{}/logs", tenantId);
        return ResponseEntity.ok(
                auditService.getLogsByTenant(tenantId, pageable));
    }

    @GetMapping("/tenants/{tenantId}/logs/event-type/{eventType}")
    @Operation(summary = "Get audit logs filtered by event type")
    public ResponseEntity<Page<AuditLogResponse>> getLogsByEventType(
            @PathVariable String tenantId,
            @PathVariable String eventType,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("GET /api/v1/audit/tenants/{}/logs/event-type/{}",
                tenantId, eventType);
        return ResponseEntity.ok(
                auditService.getLogsByTenantAndEventType(
                        tenantId, eventType, pageable));
    }

    @GetMapping("/tenants/{tenantId}/logs/source/{source}")
    @Operation(summary = "Get audit logs filtered by event source")
    public ResponseEntity<Page<AuditLogResponse>> getLogsBySource(
            @PathVariable String tenantId,
            @PathVariable String source,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("GET /api/v1/audit/tenants/{}/logs/source/{}",
                tenantId, source);
        return ResponseEntity.ok(
                auditService.getLogsByTenantAndSource(
                        tenantId, source, pageable));
    }

    @GetMapping("/tenants/{tenantId}/logs/users/{userId}")
    @Operation(summary = "Get audit logs for a specific user")
    public ResponseEntity<Page<AuditLogResponse>> getLogsByUser(
            @PathVariable String tenantId,
            @PathVariable String userId,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("GET /api/v1/audit/tenants/{}/logs/users/{}",
                tenantId, userId);
        return ResponseEntity.ok(
                auditService.getLogsByTenantAndUser(
                        tenantId, userId, pageable));
    }

    @GetMapping("/tenants/{tenantId}/logs/date-range")
    @Operation(summary = "Get audit logs within a date range")
    public ResponseEntity<Page<AuditLogResponse>> getLogsByDateRange(
            @PathVariable String tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("GET /api/v1/audit/tenants/{}/logs/date-range", tenantId);
        return ResponseEntity.ok(
                auditService.getLogsByTenantAndDateRange(
                        tenantId, from, to, pageable));
    }

    @GetMapping("/tenants/{tenantId}/logs/count")
    @Operation(summary = "Get total audit log count for a tenant")
    public ResponseEntity<Long> countLogsByTenant(
            @PathVariable String tenantId) {

        log.info("GET /api/v1/audit/tenants/{}/logs/count", tenantId);
        return ResponseEntity.ok(
                auditService.countLogsByTenant(tenantId));
    }
}
