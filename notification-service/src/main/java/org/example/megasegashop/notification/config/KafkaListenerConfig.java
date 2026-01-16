package org.example.megasegashop.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.megasegashop.notification.event.OrderCancelledEvent;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaListenerConfig {
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCancelledEvent>
            orderCancelledKafkaListenerContainerFactory(
                    KafkaProperties kafkaProperties,
                    ObjectMapper objectMapper
            ) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties(null));
        // Avoid double-configuring JsonDeserializer via properties and setters.
        props.keySet().removeIf(key -> key.startsWith("spring.json."));
        JsonDeserializer<OrderCancelledEvent> deserializer =
                new JsonDeserializer<>(OrderCancelledEvent.class, objectMapper);
        deserializer.addTrustedPackages("org.example.megasegashop.notification.event");

        DefaultKafkaConsumerFactory<String, OrderCancelledEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);

        ConcurrentKafkaListenerContainerFactory<String, OrderCancelledEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
