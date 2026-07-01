package com.tenantflow.tenant.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantEvent {

    private String eventId;
    private String eventType;
    private String tenantId;
    private String tenantName;
    private String subdomain;
    private String plan;
    private String status;
    private LocalDateTime occurredAt;

    // Factory methods — clean way to build events
    public static TenantEvent created(String tenantId, String tenantName,
                                      String subdomain, String plan, String status) {
        return TenantEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("TENANT_CREATED")
                .tenantId(tenantId)
                .tenantName(tenantName)
                .subdomain(subdomain)
                .plan(plan)
                .status(status)
                .occurredAt(LocalDateTime.now())
                .build();
    }

    public static TenantEvent updated(String tenantId, String tenantName,
                                      String subdomain, String plan, String status) {
        return TenantEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("TENANT_UPDATED")
                .tenantId(tenantId)
                .tenantName(tenantName)
                .subdomain(subdomain)
                .plan(plan)
                .status(status)
                .occurredAt(LocalDateTime.now())
                .build();
    }

    public static TenantEvent suspended(String tenantId, String tenantName,
                                        String subdomain, String plan) {
        return TenantEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("TENANT_SUSPENDED")
                .tenantId(tenantId)
                .tenantName(tenantName)
                .subdomain(subdomain)
                .plan(plan)
                .status("SUSPENDED")
                .occurredAt(LocalDateTime.now())
                .build();
    }

    public static TenantEvent deleted(String tenantId, String tenantName,
                                      String subdomain, String plan) {
        return TenantEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("TENANT_DELETED")
                .tenantId(tenantId)
                .tenantName(tenantName)
                .subdomain(subdomain)
                .plan(plan)
                .status("DELETED")
                .occurredAt(LocalDateTime.now())
                .build();
    }
}
