// billing-service/src/main/java/com/tenantflow/billing/service/UsageRecordService.java

package com.tenantflow.billing.service;

import com.tenantflow.billing.dto.response.UsageSummaryResponse;
import com.tenantflow.billing.kafka.event.ApiUsageEvent;
import java.util.UUID;

public interface UsageRecordService {
    void recordUsage(ApiUsageEvent event);
    UsageSummaryResponse getUsageSummary(UUID tenantId);
}
