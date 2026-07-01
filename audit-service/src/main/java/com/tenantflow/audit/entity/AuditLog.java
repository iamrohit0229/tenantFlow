package com.tenantflow.audit.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

// MongoDB collection — NOT a SQL table!
// One collection stores ALL audit events from ALL services
// Tenant isolation via tenantId field on every document
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_logs")
public class AuditLog {

    @Id
    private String id;                  // MongoDB ObjectId (String)

    @Indexed                            // Fast lookup by eventId
    private String eventId;             // Original eventId from producer

    @Indexed                            // Fast lookup by tenantId
    private String tenantId;

    private String userId;              // null for tenant events

    private String eventType;           // TENANT_CREATED, USER_LOGGED_IN etc.

    private String eventSource;         // tenant-service, identity-service etc.

    private String email;               // null for tenant events

    private String role;                // null for tenant events

    private String tenantName;          // null for auth events

    private String subdomain;           // null for auth events

    private String plan;                // null for auth events

    private String status;              // tenant status or account status

    private Object additionalData;      // any extra fields from event

    // Immutable timestamp — set once, never updated!
    // WHY: Audit logs must be tamper-proof
    @Indexed                            // Fast range queries by date
    private LocalDateTime occurredAt;

    private LocalDateTime receivedAt;   // when audit-service received it
}
