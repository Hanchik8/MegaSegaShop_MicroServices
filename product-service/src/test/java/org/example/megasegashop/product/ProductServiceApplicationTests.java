package org.example.megasegashop.product;

import org.example.megasegashop.product.event.ProductCreatedEvent;
import org.example.megasegashop.product.event.ProductDeletedEvent;
import org.example.megasegashop.product.event.ProductUpdatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootTest(properties = {
        "spring.cache.type=none",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
class ProductServiceApplicationTests {

    @MockBean
    private KafkaTemplate<String, ProductCreatedEvent> productCreatedKafkaTemplate;

    @MockBean
    private KafkaTemplate<String, ProductUpdatedEvent> productUpdatedKafkaTemplate;

    @MockBean
    private KafkaTemplate<String, ProductDeletedEvent> productDeletedKafkaTemplate;

    @Test
    void contextLoads() {
    }

}
