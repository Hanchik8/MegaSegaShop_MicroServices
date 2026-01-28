package org.example.megasegashop.order;

import org.example.megasegashop.order.event.OrderCancelledEvent;
import org.example.megasegashop.order.event.OrderPlacedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootTest
class OrderServiceApplicationTests {
    @MockBean
    private KafkaTemplate<String, OrderPlacedEvent> orderPlacedKafkaTemplate;

    @MockBean
    private KafkaTemplate<String, OrderCancelledEvent> orderCancelledKafkaTemplate;

    @Test
    void contextLoads() {
    }
}
