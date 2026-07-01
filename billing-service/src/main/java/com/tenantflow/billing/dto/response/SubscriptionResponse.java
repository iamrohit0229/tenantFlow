// billing-service/src/main/java/com/tenantflow/billing/dto/response/SubscriptionResponse.java

package com.tenantflow.billing.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {

    private UUID id;
    private UUID tenantId;
    private String plan;
    private String status;
    private Long quotaLimit;
    private LocalDate billingCycleStart;
    private LocalDate billingCycleEnd;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
