// billing-service/src/main/java/com/tenantflow/billing/entity/Subscription.java

package com.tenantflow.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, unique = true)
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus status;

    @Column(name = "quota_limit", nullable = false)
    private Long quotaLimit;

    @Column(name = "billing_cycle_start", nullable = false)
    private LocalDate billingCycleStart;

    @Column(name = "billing_cycle_end", nullable = false)
    private LocalDate billingCycleEnd;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (billingCycleStart == null) billingCycleStart = LocalDate.now();
        if (billingCycleEnd == null) billingCycleEnd = LocalDate.now().plusDays(30);
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Plan {
        FREE, PRO, ENTERPRISE;

        public long getQuotaLimit() {
            return switch (this) {
                case FREE -> 1_000L;
                case PRO -> 50_000L;
                case ENTERPRISE -> Long.MAX_VALUE; // unlimited
            };
        }

        public long getMonthlyPriceCents() {
            return switch (this) {
                case FREE -> 0L;
                case PRO -> 4900L;        // $49.00
                case ENTERPRISE -> 49900L; // $499.00
            };
        }
    }

    public enum SubscriptionStatus {
        ACTIVE, CANCELLED, SUSPENDED, PAST_DUE
    }
}
