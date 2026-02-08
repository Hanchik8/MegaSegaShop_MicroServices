package org.example.megasegashop.order.service;

import org.example.megasegashop.order.client.CartClient;
import org.example.megasegashop.order.client.InventoryClient;
import org.example.megasegashop.order.client.UserProfileClient;
import org.example.megasegashop.order.dto.CartItemSnapshot;
import org.example.megasegashop.order.dto.CartSnapshot;
import org.example.megasegashop.order.dto.PlaceOrderRequest;
import org.example.megasegashop.order.dto.OrderResponse;
import org.example.megasegashop.order.dto.UserProfileSnapshot;
import org.example.megasegashop.order.entity.Order;
import org.example.megasegashop.order.entity.OrderStatus;
import org.example.megasegashop.order.event.OrderCancelledEvent;
import org.example.megasegashop.order.event.OrderPlacedEvent;
import org.example.megasegashop.order.repository.OrderRepository;
import org.example.megasegashop.shared.dto.InventoryReserveResponse;
import org.springframework.http.HttpStatus;
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
import java.util.List;

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

    @Test
    void placeOrder_whenReserveFails_doesNotCallCompensationRelease() {
        Long userId = 10L;
        when(cartClient.getCart(userId)).thenReturn(new CartSnapshot(
                userId,
                List.of(new CartItemSnapshot(101L, "Game", BigDecimal.TEN, 1)),
                BigDecimal.TEN
        ));
        when(inventoryClient.reserve(any())).thenReturn(new InventoryReserveResponse(false, "Insufficient stock"));

        PlaceOrderRequest request = new PlaceOrderRequest(userId, "reserve-fail@test.com");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> orderService.placeOrder(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(inventoryClient, never()).release(any());
    }

    @Test
    void placeOrder_whenOrderConstructionFails_afterReserve_callsCompensationRelease() {
        Long userId = 11L;
        when(cartClient.getCart(userId)).thenReturn(new CartSnapshot(
                userId,
                List.of(new CartItemSnapshot(202L, "Console", null, 1)),
                BigDecimal.valueOf(100)
        ));
        when(userProfileClient.getByAuthUserId(userId))
                .thenReturn(new UserProfileSnapshot(1L, userId, "persist-fail@test.com", null));
        when(inventoryClient.reserve(any())).thenReturn(new InventoryReserveResponse(true, "Reserved"));
        when(inventoryClient.release(any())).thenReturn(new InventoryReserveResponse(true, "Released"));

        PlaceOrderRequest request = new PlaceOrderRequest(userId, "persist-fail@test.com");

        assertThrows(ResponseStatusException.class, () -> orderService.placeOrder(request));

        verify(inventoryClient).release(any());
    }

    @Test
    void cancelOrder_success_marksCancellingThenCancelled() {
        Order order = new Order();
        order.setUserId(12L);
        order.setEmail("cancel@test.com");
        order.setStatus(OrderStatus.PLACED);
        order.setTotalAmount(BigDecimal.valueOf(50));
        Order saved = orderRepository.save(order);

        when(inventoryClient.release(any())).thenReturn(new InventoryReserveResponse(true, "Released"));

        OrderResponse response = orderService.cancelOrder(saved.getId());

        assertEquals(OrderStatus.CANCELLED, response.status());
        Order reloaded = orderRepository.findById(saved.getId()).orElseThrow();
        assertEquals(OrderStatus.CANCELLED, reloaded.getStatus());
    }
}
