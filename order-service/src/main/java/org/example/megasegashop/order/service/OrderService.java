package org.example.megasegashop.order.service;

import org.example.megasegashop.order.client.CartClient;
import org.example.megasegashop.order.client.InventoryClient;
import org.example.megasegashop.order.client.UserProfileClient;
import org.example.megasegashop.order.dto.CartItemSnapshot;
import org.example.megasegashop.order.dto.CartSnapshot;
import org.example.megasegashop.shared.dto.InventoryItemRequest;
import org.example.megasegashop.shared.dto.InventoryReserveRequest;
import org.example.megasegashop.shared.dto.InventoryReserveResponse;
import org.example.megasegashop.order.dto.OrderItemResponse;
import org.example.megasegashop.order.dto.OrderResponse;
import org.example.megasegashop.order.dto.PlaceOrderRequest;
import org.example.megasegashop.order.dto.UserProfileSnapshot;
import org.example.megasegashop.order.entity.OrderStatus;
import org.example.megasegashop.order.entity.Order;
import org.example.megasegashop.order.entity.OrderItem;
import org.example.megasegashop.order.event.OrderCancelledEvent;
import org.example.megasegashop.order.event.OrderPlacedEvent;
import org.example.megasegashop.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OrderService {
    private static final String ORDER_PLACED_TOPIC = "order.placed";
    private static final String ORDER_CANCELLED_TOPIC = "order.cancelled";

    private final CartClient cartClient;
    private final InventoryClient inventoryClient;
    private final UserProfileClient userProfileClient;
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderPlacedEvent> orderPlacedKafkaTemplate;
    private final KafkaTemplate<String, OrderCancelledEvent> orderCancelledKafkaTemplate;

    public OrderService(
            CartClient cartClient,
            InventoryClient inventoryClient,
            UserProfileClient userProfileClient,
            OrderRepository orderRepository,
            KafkaTemplate<String, OrderPlacedEvent> orderPlacedKafkaTemplate,
            KafkaTemplate<String, OrderCancelledEvent> orderCancelledKafkaTemplate
    ) {
        this.cartClient = cartClient;
        this.inventoryClient = inventoryClient;
        this.userProfileClient = userProfileClient;
        this.orderRepository = orderRepository;
        this.orderPlacedKafkaTemplate = orderPlacedKafkaTemplate;
        this.orderCancelledKafkaTemplate = orderCancelledKafkaTemplate;
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
        String phone = resolvePhone(request.userId());

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
        try {
            // Step 3: Create and save order
            Order order = createOrder(request, cart);
            Order saved = orderRepository.save(order);

            // Step 4: Send Kafka event (async, but we catch exceptions)
            try {
                orderPlacedKafkaTemplate.send(ORDER_PLACED_TOPIC, 
                        new OrderPlacedEvent(saved.getId(), request.email(), phone, saved.getTotalAmount()))
                        .get(5, TimeUnit.SECONDS);
            } catch (Exception kafkaEx) {
                log.error("Failed to send Kafka event for order {}: {}", saved.getId(), kafkaEx.getMessage());
                // Kafka failure is not critical for order success - order is saved
                // In production, consider using Transactional Outbox pattern
            }

            // Step 5: Clear cart
            try {
                cartClient.clearCart(request.userId());
            } catch (Exception cartEx) {
                log.error("Failed to clear cart for user {}: {}", request.userId(), cartEx.getMessage());
                // Cart clearing failure is not critical - order is already placed
            }

            return toResponse(saved);

        } catch (Exception ex) {
            // Compensating transaction: release reserved inventory
            log.warn("Order creation failed, executing compensating transaction to release inventory");
            try {
                inventoryClient.release(reserveRequest);
                log.info("Inventory released successfully");
            } catch (Exception releaseEx) {
                log.error("CRITICAL: Failed to release inventory during compensation: {}", 
                        releaseEx.getMessage());
                // In production, this should trigger an alert and manual intervention
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
        order.setStatus(OrderStatus.PLACED);
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

    private String resolvePhone(Long userId) {
        try {
            UserProfileSnapshot profile = userProfileClient.getByAuthUserId(userId);
            if (profile != null && profile.phone() != null && !profile.phone().isBlank()) {
                return profile.phone();
            }
        } catch (Exception ex) {
            log.warn("Unable to resolve phone for user {}: {}", userId, ex.getMessage());
        }
        return null;
    }

    public OrderResponse getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        return toResponse(order);
    }

    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        if (newStatus == OrderStatus.CANCELLED) {
            return cancelOrder(orderId);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order is cancelled");
        }

        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return toResponse(order);
        }
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Delivered orders cannot be cancelled");
        }

        InventoryReserveRequest releaseRequest = buildInventoryReleaseRequest(order);
        try {
            inventoryClient.release(releaseRequest);
        } catch (Exception ex) {
            log.error("Failed to release inventory for order {}: {}", orderId, ex.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Inventory service unavailable");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);

        try {
            String phone = resolvePhone(saved.getUserId());
            orderCancelledKafkaTemplate.send(
                    ORDER_CANCELLED_TOPIC,
                    new OrderCancelledEvent(
                            saved.getId(),
                            saved.getUserId(),
                            saved.getEmail(),
                            phone,
                            saved.getTotalAmount()
                    )
            ).get(5, TimeUnit.SECONDS);
        } catch (Exception kafkaEx) {
            log.error("Failed to send order cancellation event for order {}: {}", saved.getId(), kafkaEx.getMessage());
        }

        return toResponse(saved);
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

    private InventoryReserveRequest buildInventoryReleaseRequest(Order order) {
        List<InventoryItemRequest> items = order.getItems().stream()
                .map(item -> new InventoryItemRequest(item.getProductId(), item.getQuantity()))
                .toList();
        return new InventoryReserveRequest(items);
    }
}
