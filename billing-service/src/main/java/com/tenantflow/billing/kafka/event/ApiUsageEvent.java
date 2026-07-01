package com.tenantflow.billing.kafka.event;

import lombok.*;
import java.time.LocalDateTime;

// This is the event published by API Gateway
// Billing service consumes this from Kafka
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiUsageEvent {

    private String eventId;       // unique ID for idempotency
    private String tenantId;      // which tenant made the call
    private String endpoint;      // which API endpoint was called
    private String httpMethod;    // GET POST PUT DELETE
    private Integer statusCode;   // HTTP response status
    private Long responseTimeMs;  // how long it took
    private LocalDateTime occurredAt; // when it happened
}
