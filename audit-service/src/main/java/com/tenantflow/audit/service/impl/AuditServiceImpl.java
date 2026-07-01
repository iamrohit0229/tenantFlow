package com.tenantflow.audit.service.impl;

import com.tenantflow.audit.dto.response.AuditLogResponse;
import com.tenantflow.audit.repository.AuditLogRepository;
import com.tenantflow.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public Page<AuditLogResponse> getLogsByTenant(
            String tenantId, Pageable pageable) {
        log.debug("Fetching audit logs for tenantId: {}", tenantId);
        return auditLogRepository
                .findAllByTenantId(tenantId, pageable)
                .map(AuditLogResponse::from);
    }

    @Override
    public Page<AuditLogResponse> getLogsByTenantAndEventType(
            String tenantId, String eventType, Pageable pageable) {
        log.debug("Fetching audit logs for tenantId: {} eventType: {}",
                tenantId, eventType);
        return auditLogRepository
                .findAllByTenantIdAndEventType(tenantId, eventType, pageable)
                .map(AuditLogResponse::from);
    }

    @Override
    public Page<AuditLogResponse> getLogsByTenantAndSource(
            String tenantId, String source, Pageable pageable) {
        log.debug("Fetching audit logs for tenantId: {} source: {}",
                tenantId, source);
        return auditLogRepository
                .findAllByTenantIdAndEventSource(tenantId, source, pageable)
                .map(AuditLogResponse::from);
    }

    @Override
    public Page<AuditLogResponse> getLogsByTenantAndUser(
            String tenantId, String userId, Pageable pageable) {
        log.debug("Fetching audit logs for tenantId: {} userId: {}",
                tenantId, userId);
        return auditLogRepository
                .findAllByTenantIdAndUserId(tenantId, userId, pageable)
                .map(AuditLogResponse::from);
    }

    @Override
    public Page<AuditLogResponse> getLogsByTenantAndDateRange(
            String tenantId, LocalDateTime from,
            LocalDateTime to, Pageable pageable) {
        log.debug("Fetching audit logs for tenantId: {} from: {} to: {}",
                tenantId, from, to);
        return auditLogRepository
                .findAllByTenantIdAndOccurredAtBetween(
                        tenantId, from, to, pageable)
                .map(AuditLogResponse::from);
    }

    @Override
    public long countLogsByTenant(String tenantId) {
        return auditLogRepository.countByTenantId(tenantId);
    }
}
