// src/main/java/com/tenantflow/tenant/repository/TenantRepository.java

package com.tenantflow.tenant.repository;

import com.tenantflow.tenant.entity.Tenant;
import com.tenantflow.tenant.entity.SubscriptionPlan;
import com.tenantflow.tenant.entity.TenantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    // ─────────────────────────────────────────
    // Find Methods (exclude soft deleted)
    // ─────────────────────────────────────────

    Optional<Tenant> findByIdAndDeletedAtIsNull(UUID id);

    Optional<Tenant> findBySubdomainAndDeletedAtIsNull(String subdomain);

    Optional<Tenant> findByNameAndDeletedAtIsNull(String name);

    Page<Tenant> findAllByDeletedAtIsNull(Pageable pageable);

    // ─────────────────────────────────────────
    // Existence Checks
    // ─────────────────────────────────────────

    boolean existsByNameAndDeletedAtIsNull(String name);

    boolean existsBySubdomainAndDeletedAtIsNull(String subdomain);

    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, UUID id);

    boolean existsBySubdomainAndIdNotAndDeletedAtIsNull(String subdomain, UUID id);

    // ─────────────────────────────────────────
    // Filter By Status and Plan
    // ─────────────────────────────────────────

    Page<Tenant> findAllByStatusAndDeletedAtIsNull(
            TenantStatus status,
            Pageable pageable
    );

    Page<Tenant> findAllByPlanAndDeletedAtIsNull(
            SubscriptionPlan plan,
            Pageable pageable
    );

    Page<Tenant> findAllByStatusAndPlanAndDeletedAtIsNull(
            TenantStatus status,
            SubscriptionPlan plan,
            Pageable pageable
    );

    // ─────────────────────────────────────────
    // Usage Reset (Scheduled Job)
    // ─────────────────────────────────────────

    @Modifying
    @Query("""
            UPDATE Tenant t
            SET t.currentUsageCount = 0,
                t.lastResetDate = :today
            WHERE t.lastResetDate < :today
            AND t.deletedAt IS NULL
            """)
    int resetDailyUsageForAllTenants(@Param("today") LocalDate today);

    // ─────────────────────────────────────────
    // Stats Queries (for dashboard)
    // ─────────────────────────────────────────

    long countByDeletedAtIsNull();

    long countByStatusAndDeletedAtIsNull(TenantStatus status);

    long countByPlanAndDeletedAtIsNull(SubscriptionPlan plan);

    @Query("""
            SELECT t FROM Tenant t
            WHERE t.deletedAt IS NULL
            AND t.plan != com.tenantflow.tenant.entity.SubscriptionPlan.ENTERPRISE
            AND t.currentUsageCount >= t.quotaLimit
            """)
    List<Tenant> findTenantsWithExceededQuota();

    @Query("""
            SELECT t FROM Tenant t
            WHERE t.deletedAt IS NULL
            AND t.currentUsageCount >= (t.quotaLimit * 0.8)
            AND t.plan != com.tenantflow.tenant.entity.SubscriptionPlan.ENTERPRISE
            """)
    List<Tenant> findTenantsApproachingQuota();
}
