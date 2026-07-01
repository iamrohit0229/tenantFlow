// billing-service/src/main/java/com/tenantflow/billing/repository/UsageRecordRepository.java

package com.tenantflow.billing.repository;

import com.tenantflow.billing.entity.UsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface UsageRecordRepository extends JpaRepository<UsageRecord, UUID> {

    // Check if Kafka event already processed — idempotency check
    boolean existsByEventId(String eventId);

    // Count API calls for a tenant on a specific date
    long countByTenantIdAndUsageDate(UUID tenantId, LocalDate usageDate);

    // Count API calls for a tenant in a date range — for invoice generation
    @Query("""
            SELECT COUNT(u) FROM UsageRecord u
            WHERE u.tenantId = :tenantId
            AND u.usageDate BETWEEN :startDate AND :endDate
            """)
    long countByTenantIdAndDateRange(
            @Param("tenantId") UUID tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
