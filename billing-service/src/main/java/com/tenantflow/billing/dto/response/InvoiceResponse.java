package com.tenantflow.billing.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {

    private UUID id;
    private UUID tenantId;
    private String invoiceNumber;
    private String status;
    private String plan;
    private Long totalApiCalls;
    private Long amountCents;
    private Double amountDollars; // amountCents / 100
    private String currency;
    private LocalDate billingPeriodStart;
    private LocalDate billingPeriodEnd;
    private LocalDateTime issuedAt;
    private LocalDateTime dueAt;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
