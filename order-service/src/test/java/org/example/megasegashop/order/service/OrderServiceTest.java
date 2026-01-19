package org.example.megasegashop.order.service;

import org.example.megasegashop.order.client.CartClient;
import org.example.megasegashop.order.client.InventoryClient;
import org.example.megasegashop.order.client.UserProfileClient;
import org.example.megasegashop.order.dto.CartSnapshot;
import org.example.megasegashop.order.dto.PlaceOrderRequest;
import org.example.megasegashop.order.entity.Order;
import org.example.megasegashop.order.entity.OrderStatus;
import org.example.megasegashop.order.event.OrderCancelledEvent;
import org.example.megasegashop.order.event.OrderPlacedEvent;
import org.example.megasegashop.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private CartClient cartClient;

    @MockBean
    private InventoryClient inventoryClient;

    @MockBean
    private UserProfileClient userProfileClient;

    @MockBean
    private KafkaTemplate<String, OrderPlacedEvent> orderPlacedKafkaTemplate;

    @MockBean
    private KafkaTemplate<String, OrderCancelledEvent> orderCancelledKafkaTemplate;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    void placeOrder_withEmptyCart_throwsBadRequest() {
        // Given
        Long userId = 1L;
        when(cartClient.getCart(userId)).thenReturn(new CartSnapshot(userId, Collections.emptyList(), BigDecimal.ZERO));

        PlaceOrderRequest request = new PlaceOrderRequest(userId, "test@test.com");

        // When/Then
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.placeOrder(request)
        );
        assertTrue(exception.getMessage().contains("Cart is empty"));
    }

    @Test
    void cancelOrder_whenDelivered_throwsBadRequest() {
        // Given
        Order order = new Order();
        order.setUserId(1L);
        order.setEmail("test@test.com");
        order.setStatus(OrderStatus.DELIVERED);
        order.setTotalAmount(BigDecimal.valueOf(100));
        order.setItems(new ArrayList<>());
        Order saved = orderRepository.save(order);

        // When/Then
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.cancelOrder(saved.getId())
        );
        assertTrue(exception.getMessage().contains("Delivered orders cannot be cancelled"));
    }
}
