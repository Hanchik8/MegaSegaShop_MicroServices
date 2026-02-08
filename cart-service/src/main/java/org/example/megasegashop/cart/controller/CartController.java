package org.example.megasegashop.cart.controller;

import jakarta.validation.Valid;
import org.example.megasegashop.cart.dto.AddCartItemRequest;
import org.example.megasegashop.cart.dto.CartResponse;
import org.example.megasegashop.cart.dto.RemoveCartItemRequest;
import org.example.megasegashop.cart.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{userId}")
    public CartResponse getCart(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) String authenticatedUserId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        // Verify user is accessing their own cart (unless admin)
        validateUserAccess(userId, authenticatedUserId, userRole);
        return cartService.getCart(userId);
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @Valid @RequestBody AddCartItemRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String authenticatedUserId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        // Verify user is modifying their own cart
        validateUserAccess(request.userId(), authenticatedUserId, userRole);
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(request));
    }

    @DeleteMapping("/items")
    public CartResponse removeItem(
            @Valid @RequestBody RemoveCartItemRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String authenticatedUserId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        // Verify user is modifying their own cart
        validateUserAccess(request.userId(), authenticatedUserId, userRole);
        return cartService.removeItem(request);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) String authenticatedUserId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        // Allow clearing own cart or internal service calls (no headers = internal)
        // Internal service calls from order-service won't have X-User-Id header
        if (authenticatedUserId != null) {
            validateUserAccess(userId, authenticatedUserId, userRole);
        }
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
    
    private void validateUserAccess(Long targetUserId, String authenticatedUserId, String role) {
        // Internal service calls don't have user headers - allow them
        if (authenticatedUserId == null) {
            return;
        }
        // Admins can access any cart
        if (isAdmin(role)) {
            return;
        }
        // Regular users can only access their own cart
        try {
            Long authId = Long.parseLong(authenticatedUserId);
            if (!authId.equals(targetUserId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid user ID");
        }
    }
    
    private boolean isAdmin(String role) {
        return role != null && (role.equals("ROLE_ADMIN") || role.equals("ADMIN"));
    }
}


