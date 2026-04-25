package com.tenantflow.billing.kafka;

import com.tenantflow.billing.kafka.event.ApiUsageEvent;
import com.tenantflow.billing.service.UsageRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingEventConsumer {

    private final UsageRecordService usageRecordService;

    @KafkaListener(
            topics = "api.usage.events",
            groupId = "billing-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeApiUsageEvent(
            @Payload ApiUsageEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.debug("Received usage event: eventId={} tenantId={} topic={} partition={} offset={}",
                event.getEventId(), event.getTenantId(), topic, partition, offset);

        try {
            usageRecordService.recordUsage(event);
        } catch (Exception e) {
            log.error("Failed to process usage event: eventId={} error={}",
                    event.getEventId(), e.getMessage(), e);
            // In production: send to dead letter topic
        }
    }
}
