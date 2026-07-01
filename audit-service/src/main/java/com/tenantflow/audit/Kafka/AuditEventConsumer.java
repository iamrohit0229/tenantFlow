package com.tenantflow.audit.kafka;

import com.tenantflow.audit.entity.AuditLog;
import com.tenantflow.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventConsumer {

    private final AuditLogRepository auditLogRepository;

    // ─────────────────────────────────────────
    // TENANT EVENTS
    // ─────────────────────────────────────────

    @KafkaListener(
            topics = "tenant.events",
            groupId = "audit-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeTenantEvent(Map<String, Object> event) {
        log.debug("Received tenant event: {}", event.get("eventType"));

        String eventId = getString(event, "eventId");

        // Idempotency check — never process same event twice!
        if (auditLogRepository.existsByEventId(eventId)) {
            log.warn("Duplicate tenant event [{}] — skipping", eventId);
            return;
        }

        AuditLog auditLog = AuditLog.builder()
                .eventId(eventId)
                .eventType(getString(event, "eventType"))
                .eventSource("tenant-service")
                .tenantId(getString(event, "tenantId"))
                .tenantName(getString(event, "tenantName"))
                .subdomain(getString(event, "subdomain"))
                .plan(getString(event, "plan"))
                .status(getString(event, "status"))
                .occurredAt(parseDateTime(event, "occurredAt"))
                .receivedAt(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);

        log.info("Audit log saved for tenant event [{}] tenantId [{}]",
                auditLog.getEventType(), auditLog.getTenantId());
    }

    // ─────────────────────────────────────────
    // AUTH EVENTS
    // ─────────────────────────────────────────

    @KafkaListener(
            topics = "auth.events",
            groupId = "audit-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeAuthEvent(Map<String, Object> event) {
        log.debug("Received auth event: {}", event.get("eventType"));

        String eventId = getString(event, "eventId");

        // Idempotency check
        if (auditLogRepository.existsByEventId(eventId)) {
            log.warn("Duplicate auth event [{}] — skipping", eventId);
            return;
        }

        AuditLog auditLog = AuditLog.builder()
                .eventId(eventId)
                .eventType(getString(event, "eventType"))
                .eventSource("identity-service")
                .tenantId(getString(event, "tenantId"))
                .userId(getString(event, "userId"))
                .email(getString(event, "email"))
                .role(getString(event, "role"))
                .occurredAt(parseDateTime(event, "timestamp"))
                .receivedAt(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);

        log.info("Audit log saved for auth event [{}] userId [{}]",
                auditLog.getEventType(), auditLog.getUserId());
    }

    // ─────────────────────────────────────────
    // Private Helpers
    // ─────────────────────────────────────────

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }

    private LocalDateTime parseDateTime(Map<String, Object> map, String key) {
        try {
            Object val = map.get(key);
            if (val == null) return LocalDateTime.now();
            return LocalDateTime.parse(val.toString());
        } catch (Exception e) {
            log.warn("Could not parse datetime for key [{}] — using now()", key);
            return LocalDateTime.now();
        }
    }
}
