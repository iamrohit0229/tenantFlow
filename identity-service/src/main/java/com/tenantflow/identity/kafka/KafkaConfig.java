package com.tenantflow.identity.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    // Creates topic automatically if it does not exist
    // partitions = 3 → parallel processing per tenant
    // replicas = 1 → single broker for dev (increase for prod)
    @Bean
    public NewTopic authEventsTopic() {
        return TopicBuilder.name("auth.events")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
