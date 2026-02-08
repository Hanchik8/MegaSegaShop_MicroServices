package org.example.megasegashop.order.controller;

import jakarta.validation.Valid;
import org.example.megasegashop.order.dto.OrderResponse;
import org.example.megasegashop.order.dto.PlaceOrderRequest;
import org.example.megasegashop.order.dto.UpdateOrderStatusRequest;
import org.example.megasegashop.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.example.megasegashop.shared.web.AdminOnly;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail
    ) {
        // Validate that the order email matches the authenticated user
        if (userEmail != null && !userEmail.equalsIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot place order for different user");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(request));
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(
            @PathVariable Long orderId,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        OrderResponse order = orderService.getOrder(orderId);
        // Allow admins to view any order, but regular users can only view their own orders
        if (userEmail != null && !isAdmin(userRole) && !userEmail.equalsIgnoreCase(order.email())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return order;
    }

    @GetMapping("/user/{userId}")
    public List<OrderResponse> getOrdersByUser(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        // Admins can view any user's orders
        // For security, non-admins should only be able to see their own orders
        // This would require userId-to-email mapping; for now, we restrict to admin-only
        if (!isAdmin(userRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
        return orderService.getOrdersByUserId(userId);
    }

    @AdminOnly
    @PatchMapping("/{orderId}/status")
    public OrderResponse updateStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        return orderService.updateStatus(orderId, request.status());
    }

    @PostMapping("/{orderId}/cancel")
    public OrderResponse cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        OrderResponse order = orderService.getOrder(orderId);
        // Allow admins or the order owner to cancel
        if (userEmail != null && !isAdmin(userRole) && !userEmail.equalsIgnoreCase(order.email())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return orderService.cancelOrder(orderId);
    }
    
    private boolean isAdmin(String role) {
        return role != null && (role.equals("ROLE_ADMIN") || role.equals("ADMIN"));
    }
}

