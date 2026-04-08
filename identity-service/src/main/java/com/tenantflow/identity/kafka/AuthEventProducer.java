package com.tenantflow.identity.kafka;

import com.tenantflow.identity.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String AUTH_EVENTS_TOPIC = "auth.events";

    // --- User Registered ---

    public void publishUserRegisteredEvent(User user) {
        Map<String, Object> event = buildBaseEvent(user, "USER_REGISTERED");
        publishEvent(event, user.getTenantId().toString());
        log.info("Published USER_REGISTERED event for user: {}", user.getId());
    }

    // --- User Logged In ---

    public void publishUserLoggedInEvent(User user) {
        Map<String, Object> event = buildBaseEvent(user, "USER_LOGGED_IN");
        publishEvent(event, user.getTenantId().toString());
        log.info("Published USER_LOGGED_IN event for user: {}", user.getId());
    }

    // --- User Logged Out ---

    public void publishUserLoggedOutEvent(User user) {
        Map<String, Object> event = buildBaseEvent(user, "USER_LOGGED_OUT");
        publishEvent(event, user.getTenantId().toString());
        log.info("Published USER_LOGGED_OUT event for user: {}", user.getId());
    }

    // --- User Locked ---

    public void publishUserLockedEvent(User user) {
        Map<String, Object> event = buildBaseEvent(user, "USER_LOCKED");
        event.put("lockedUntil", user.getLockedUntil().toString());
        publishEvent(event, user.getTenantId().toString());
        log.warn("Published USER_LOCKED event for user: {}", user.getId());
    }

    // --- Private Helpers ---

    private Map<String, Object> buildBaseEvent(User user, String eventType) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("eventId", java.util.UUID.randomUUID().toString());
        event.put("timestamp", LocalDateTime.now().toString());
        event.put("userId", user.getId().toString());
        event.put("tenantId", user.getTenantId().toString());
        event.put("email", user.getEmail());
        event.put("role", user.getRole().name());
        return event;
    }

    private void publishEvent(Map<String, Object> event, String key) {
        // Key = tenantId ensures all events for same tenant
        // go to same Kafka partition — ordering guaranteed per tenant
        kafkaTemplate.send(AUTH_EVENTS_TOPIC, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish auth event: {}",
                                ex.getMessage());
                    } else {
                        log.debug("Auth event published to partition: {}",
                                result.getRecordMetadata().partition());
                    }
                });
    }
}
