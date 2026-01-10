package org.example.megasegashop.order.service;

import org.example.megasegashop.order.client.CartClient;
import org.example.megasegashop.order.client.InventoryClient;
import org.example.megasegashop.order.dto.CartItemSnapshot;
import org.example.megasegashop.order.dto.CartSnapshot;
import org.example.megasegashop.order.dto.InventoryItemRequest;
import org.example.megasegashop.order.dto.InventoryReserveRequest;
import org.example.megasegashop.order.dto.InventoryReserveResponse;
import org.example.megasegashop.order.dto.OrderItemResponse;
import org.example.megasegashop.order.dto.OrderResponse;
import org.example.megasegashop.order.dto.PlaceOrderRequest;
import org.example.megasegashop.order.entity.Order;
import org.example.megasegashop.order.entity.OrderItem;
import org.example.megasegashop.order.event.OrderPlacedEvent;
import org.example.megasegashop.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private static final String ORDER_PLACED_TOPIC = "order.placed";

    private final CartClient cartClient;
    private final InventoryClient inventoryClient;
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public OrderService(
            CartClient cartClient,
            InventoryClient inventoryClient,
            OrderRepository orderRepository,
            KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate
    ) {
        this.cartClient = cartClient;
        this.inventoryClient = inventoryClient;
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Places an order using Saga pattern with compensating transactions.
     * 
     * Saga steps:
     * 1. Get cart
     * 2. Reserve inventory (compensate: release inventory)
     * 3. Save order
     * 4. Send Kafka event
     * 5. Clear cart
     * 
     * If any step fails after inventory reservation, we compensate by releasing inventory.
     */
    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        // Step 1: Get cart
        CartSnapshot cart = cartClient.getCart(request.userId());
        if (cart == null || cart.items() == null || cart.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        // Build reserve request (reused for both reserve and release)
        InventoryReserveRequest reserveRequest = new InventoryReserveRequest(
                cart.items().stream()
                        .map(item -> new InventoryItemRequest(item.productId(), item.quantity()))
                        .toList()
        );

        // Step 2: Reserve inventory
        InventoryReserveResponse reserveResponse = inventoryClient.reserve(reserveRequest);
        if (!reserveResponse.success()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, reserveResponse.message());
        }

        // From this point, we need compensating transactions if anything fails
        boolean inventoryReserved = true;
        
        try {
            // Step 3: Create and save order
            Order order = createOrder(request, cart);
            Order saved = orderRepository.save(order);

            // Step 4: Send Kafka event (async, but we catch exceptions)
            try {
                kafkaTemplate.send(ORDER_PLACED_TOPIC, 
                        new OrderPlacedEvent(saved.getId(), request.email(), saved.getTotalAmount()))
                        .get(); // Wait for confirmation
            } catch (Exception kafkaEx) {
                logger.error("Failed to send Kafka event for order {}: {}", saved.getId(), kafkaEx.getMessage());
                // Kafka failure is not critical for order success - order is saved
                // In production, consider using Transactional Outbox pattern
            }

            // Step 5: Clear cart
            try {
                cartClient.clearCart(request.userId());
            } catch (Exception cartEx) {
                logger.error("Failed to clear cart for user {}: {}", request.userId(), cartEx.getMessage());
                // Cart clearing failure is not critical - order is already placed
            }

            return toResponse(saved);

        } catch (Exception ex) {
            // Compensating transaction: release reserved inventory
            if (inventoryReserved) {
                logger.warn("Order creation failed, executing compensating transaction to release inventory");
                try {
                    inventoryClient.release(reserveRequest);
                    logger.info("Inventory released successfully");
                } catch (Exception releaseEx) {
                    logger.error("CRITICAL: Failed to release inventory during compensation: {}", 
                            releaseEx.getMessage());
                    // In production, this should trigger an alert and manual intervention
                }
            }
            
            // Re-throw the original exception
            if (ex instanceof ResponseStatusException rse) {
                throw rse;
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Order creation failed: " + ex.getMessage());
        }
    }

    private Order createOrder(PlaceOrderRequest request, CartSnapshot cart) {
        Order order = new Order();
        order.setUserId(request.userId());
        order.setEmail(request.email());
        order.setStatus("PLACED");
        order.setItems(new ArrayList<>());

        BigDecimal total = BigDecimal.ZERO;
        for (CartItemSnapshot item : cart.items()) {
            BigDecimal lineTotal = item.unitPrice().multiply(BigDecimal.valueOf(item.quantity()));
            total = total.add(lineTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(item.productId());
            orderItem.setProductName(item.productName());
            orderItem.setUnitPrice(item.unitPrice());
            orderItem.setQuantity(item.quantity());
            orderItem.setOrder(order);
            order.getItems().add(orderItem);
        }
        order.setTotalAmount(total);
        
        return order;
    }

    public OrderResponse getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        return toResponse(order);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getUnitPrice(),
                        item.getQuantity()
                ))
                .toList();
        return new OrderResponse(order.getId(), order.getStatus(), order.getTotalAmount(), order.getCreatedAt(), items);
    }
}
