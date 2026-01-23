package org.example.megasegashop.cart.client;

import org.example.megasegashop.cart.dto.ProductResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback implementation for ProductClient when product-service is unavailable.
 */
@Slf4j
@Component
public class ProductClientFallback implements ProductClient {

    @Override
    public ProductResponse getProduct(Long id) {
        log.warn("Circuit breaker fallback: product-service unavailable for productId={}", id);
        return null;
    }
}
