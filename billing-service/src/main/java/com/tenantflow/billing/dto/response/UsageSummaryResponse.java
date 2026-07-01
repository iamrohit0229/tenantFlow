package com.tenantflow.billing.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageSummaryResponse {

    private UUID tenantId;
    private String plan;
    private Long quotaLimit;
    private Long usageTodayCount;
    private Long remainingQuota;
    private Double usagePercentage;
    private LocalDate resetDate;
}
