package com.tenantflow.audit.dto.response;

import com.tenantflow.audit.entity.AuditLog;
import lombok.Builder;

import java.time.LocalDateTime;

// Java Record — immutable DTO, no boilerplate needed
@Builder
public record AuditLogResponse(
        String id,
        String eventId,
        String eventType,
        String eventSource,
        String tenantId,
        String userId,
        String email,
        String role,
        String tenantName,
        String subdomain,
        String plan,
        String status,
        LocalDateTime occurredAt,
        LocalDateTime receivedAt
) {
    // Factory method — maps entity to DTO cleanly
    public static AuditLogResponse from(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .eventId(log.getEventId())
                .eventType(log.getEventType())
                .eventSource(log.getEventSource())
                .tenantId(log.getTenantId())
                .userId(log.getUserId())
                .email(log.getEmail())
                .role(log.getRole())
                .tenantName(log.getTenantName())
                .subdomain(log.getSubdomain())
                .plan(log.getPlan())
                .status(log.getStatus())
                .occurredAt(log.getOccurredAt())
                .receivedAt(log.getReceivedAt())
                .build();
    }
}
