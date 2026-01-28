package org.example.megasegashop.product.client;

import org.example.megasegashop.product.dto.InventoryItemResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback implementation for InventoryClient when inventory-service is unavailable.
 */
@Slf4j
@Component
public class InventoryClientFallback implements InventoryClient {

    @Override
    public InventoryItemResponse getInventory(Long productId) {
        log.warn("Circuit breaker fallback: inventory-service unavailable for productId={}", productId);
        // Return null to indicate unavailability - controller handles this as NOT_FOUND or SERVICE_UNAVAILABLE
        return null;
    }
}
