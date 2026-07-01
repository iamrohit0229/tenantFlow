package com.tenantflow.tenant.kafka;

import com.tenantflow.tenant.kafka.event.TenantEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantEventProducer {

    private static final String TOPIC = "tenant.events";

    private final KafkaTemplate<String, TenantEvent> kafkaTemplate;

    public void publishTenantCreated(TenantEvent event) {
        publish(event);
    }

    public void publishTenantUpdated(TenantEvent event) {
        publish(event);
    }

    public void publishTenantSuspended(TenantEvent event) {
        publish(event);
    }

    public void publishTenantDeleted(TenantEvent event) {
        publish(event);
    }

    // tenantId as key ensures all events for same
    // tenant go to same partition (ordering guaranteed)
    private void publish(TenantEvent event) {
        CompletableFuture<SendResult<String, TenantEvent>> future =
                kafkaTemplate.send(TOPIC, event.getTenantId(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event [{}] for tenant [{}]: {}",
                        event.getEventType(), event.getTenantId(), ex.getMessage());
            } else {
                log.info("Published event [{}] for tenant [{}] to partition [{}] offset [{}]",
                        event.getEventType(),
                        event.getTenantId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
