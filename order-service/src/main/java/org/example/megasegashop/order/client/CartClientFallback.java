package org.example.megasegashop.order.client;

import org.example.megasegashop.order.dto.CartSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;

/**
 * Fallback implementation for CartClient when cart-service is unavailable.
 */
@Slf4j
@Component
public class CartClientFallback implements CartClient {

    @Override
    public CartSnapshot getCart(Long userId) {
        log.warn("Circuit breaker fallback: cart-service unavailable for userId={}", userId);
        // Return empty cart to indicate unavailability
        return new CartSnapshot(userId, Collections.emptyList(), BigDecimal.ZERO);
    }

    @Override
    public void clearCart(Long userId) {
        log.warn("Circuit breaker fallback: cart-service unavailable, cannot clear cart for userId={}", userId);
        // Silent failure - order is already placed, cart clearing is not critical
    }
}
