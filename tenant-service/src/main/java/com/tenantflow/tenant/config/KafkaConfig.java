package com.tenantflow.tenant.config;

import com.tenantflow.tenant.kafka.event.TenantEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, TenantEvent> tenantEventProducerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // acks=all → wait for ALL replicas to confirm
        // Guarantees no data loss even if broker crashes
        config.put(ProducerConfig.ACKS_CONFIG, "all");

        // Retry 3 times on transient failures
        config.put(ProducerConfig.RETRIES_CONFIG, 3);

        // Idempotent producer — exactly-once delivery guarantee
        // Prevents duplicate events if retry happens after broker ack
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        // Do NOT add type headers — keeps events clean
        // audit-service deserializes by field names, not Java class type
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, TenantEvent> kafkaTemplate() {
        return new KafkaTemplate<>(tenantEventProducerFactory());
    }
}
