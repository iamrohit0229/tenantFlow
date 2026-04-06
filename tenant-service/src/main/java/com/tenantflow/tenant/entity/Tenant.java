package com.tenantflow.tenant.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "tenants",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_tenants_name",      columnNames = "name"),
                @UniqueConstraint(name = "uq_tenants_subdomain", columnNames = "subdomain")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "subdomain", nullable = false, length = 50)
    private String subdomain;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TenantStatus status = TenantStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false, length = 20)
    @Builder.Default
    private SubscriptionPlan plan = SubscriptionPlan.FREE;

    @Column(name = "quota_limit", nullable = false)
    @Builder.Default
    private Long quotaLimit = SubscriptionPlan.FREE.getQuotaLimit();

    @Column(name = "current_usage_count", nullable = false)
    @Builder.Default
    private Long currentUsageCount = 0L;

    @Column(name = "last_reset_date", nullable = false)
    @Builder.Default
    private LocalDate lastResetDate = LocalDate.now();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ─────────────────────────────────────────
    // Business Methods
    // ─────────────────────────────────────────

    public void upgradePlan(SubscriptionPlan newPlan) {
        this.plan = newPlan;
        this.quotaLimit = newPlan.getQuotaLimit();
    }

    public void incrementUsage() {
        this.currentUsageCount++;
    }

    public void resetDailyUsage() {
        this.currentUsageCount = 0L;
        this.lastResetDate = LocalDate.now();
    }

    public boolean isQuotaExceeded() {
        return plan.hasQuotaExceeded(currentUsageCount);
    }

    public boolean isAccessAllowed() {
        return status.isAccessAllowed() && !isQuotaExceeded();
    }

    public void softDelete() {
        this.status = TenantStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
