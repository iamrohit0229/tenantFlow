package com.tenantflow.audit.repository;

import com.tenantflow.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    // All logs for a tenant — tenant isolation enforced here!
    Page<AuditLog> findAllByTenantId(String tenantId, Pageable pageable);

    // Filter by event type within a tenant
    Page<AuditLog> findAllByTenantIdAndEventType(
            String tenantId, String eventType, Pageable pageable);

    // Filter by event source within a tenant
    Page<AuditLog> findAllByTenantIdAndEventSource(
            String tenantId, String eventSource, Pageable pageable);

    // Filter by userId within a tenant
    Page<AuditLog> findAllByTenantIdAndUserId(
            String tenantId, String userId, Pageable pageable);

    // Date range query within a tenant
    Page<AuditLog> findAllByTenantIdAndOccurredAtBetween(
            String tenantId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable);

    // Check if event already processed — idempotency!
    boolean existsByEventId(String eventId);

    // Count events per tenant
    long countByTenantId(String tenantId);
}
