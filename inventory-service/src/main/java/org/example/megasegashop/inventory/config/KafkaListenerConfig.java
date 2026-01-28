package org.example.megasegashop.inventory.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.megasegashop.inventory.event.ProductDeletedEvent;
import org.example.megasegashop.inventory.event.ProductUpdatedEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnBean(KafkaProperties.class)
public class KafkaListenerConfig {
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProductUpdatedEvent>
            productUpdatedKafkaListenerContainerFactory(
                    KafkaProperties kafkaProperties,
                    ObjectMapper objectMapper
            ) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties(null));
        // Avoid double-configuring JsonDeserializer via properties and setters.
        props.keySet().removeIf(key -> key.startsWith("spring.json."));
        JsonDeserializer<ProductUpdatedEvent> deserializer =
                new JsonDeserializer<>(ProductUpdatedEvent.class, objectMapper);
        deserializer.addTrustedPackages("org.example.megasegashop.inventory.event");

        DefaultKafkaConsumerFactory<String, ProductUpdatedEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);

        ConcurrentKafkaListenerContainerFactory<String, ProductUpdatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProductDeletedEvent>
            productDeletedKafkaListenerContainerFactory(
                    KafkaProperties kafkaProperties,
                    ObjectMapper objectMapper
            ) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties(null));
        // Avoid double-configuring JsonDeserializer via properties and setters.
        props.keySet().removeIf(key -> key.startsWith("spring.json."));
        JsonDeserializer<ProductDeletedEvent> deserializer =
                new JsonDeserializer<>(ProductDeletedEvent.class, objectMapper);
        deserializer.addTrustedPackages("org.example.megasegashop.inventory.event");

        DefaultKafkaConsumerFactory<String, ProductDeletedEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);

        ConcurrentKafkaListenerContainerFactory<String, ProductDeletedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
