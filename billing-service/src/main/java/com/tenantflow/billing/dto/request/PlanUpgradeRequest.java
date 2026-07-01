package com.tenantflow.billing.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanUpgradeRequest {

    @NotNull(message = "Plan is required")
    private String plan; // FREE, PRO, ENTERPRISE
}
