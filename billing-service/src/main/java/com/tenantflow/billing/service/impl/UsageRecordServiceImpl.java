package com.tenantflow.billing.service.impl;

import com.tenantflow.billing.dto.response.UsageSummaryResponse;
import com.tenantflow.billing.entity.Subscription;
import com.tenantflow.billing.entity.UsageRecord;
import com.tenantflow.billing.kafka.event.ApiUsageEvent;
import com.tenantflow.billing.repository.SubscriptionRepository;
import com.tenantflow.billing.repository.UsageRecordRepository;
import com.tenantflow.billing.service.UsageRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsageRecordServiceImpl implements UsageRecordService {

    private final UsageRecordRepository usageRecordRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    @Transactional
    public void recordUsage(ApiUsageEvent event) {

        // IDEMPOTENCY CHECK — if event already processed, skip it
        // This is the senior concept: same Kafka event never processed twice
        if (usageRecordRepository.existsByEventId(event.getEventId())) {
            log.warn("Duplicate usage event detected, skipping: eventId={}",
                    event.getEventId());
            return;
        }

        UsageRecord record = UsageRecord.builder()
                .tenantId(UUID.fromString(event.getTenantId()))
                .eventId(event.getEventId())
                .endpoint(event.getEndpoint())
                .httpMethod(event.getHttpMethod())
                .statusCode(event.getStatusCode())
                .responseTimeMs(event.getResponseTimeMs())
                .usageDate(event.getOccurredAt() != null
                        ? event.getOccurredAt().toLocalDate()
                        : LocalDate.now())
                .build();

        usageRecordRepository.save(record);

        log.debug("Usage recorded: tenantId={} endpoint={} method={}",
                event.getTenantId(), event.getEndpoint(), event.getHttpMethod());
    }

    @Override
    @Transactional(readOnly = true)
    public UsageSummaryResponse getUsageSummary(UUID tenantId) {

        Subscription subscription = subscriptionRepository
                .findByTenantId(tenantId)
                .orElse(null);

        long quotaLimit = subscription != null
                ? subscription.getQuotaLimit()
                : 1000L; // default FREE plan quota

        String plan = subscription != null
                ? subscription.getPlan().name()
                : "FREE";

        long usageToday = usageRecordRepository
                .countByTenantIdAndUsageDate(tenantId, LocalDate.now());

        long remaining = Math.max(0, quotaLimit - usageToday);

        double percentage = quotaLimit > 0
                ? (double) usageToday / quotaLimit * 100
                : 0;

        return UsageSummaryResponse.builder()
                .tenantId(tenantId)
                .plan(plan)
                .quotaLimit(quotaLimit)
                .usageTodayCount(usageToday)
                .remainingQuota(remaining)
                .usagePercentage(Math.min(100.0, percentage))
                .resetDate(LocalDate.now().plusDays(1))
                .build();
    }
}
