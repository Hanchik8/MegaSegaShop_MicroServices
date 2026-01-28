package org.example.megasegashop.order.client;

import org.example.megasegashop.shared.dto.InventoryReserveRequest;
import org.example.megasegashop.shared.dto.InventoryReserveResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback implementation for InventoryClient when inventory-service is unavailable.
 */
@Slf4j
@Component
public class InventoryClientFallback implements InventoryClient {

    @Override
    public InventoryReserveResponse reserve(InventoryReserveRequest request) {
        log.warn("Circuit breaker fallback: inventory-service unavailable, cannot reserve items");
        // Return failed reservation
        return new InventoryReserveResponse(false, "Inventory service unavailable");
    }

    @Override
    public InventoryReserveResponse release(InventoryReserveRequest request) {
        log.warn("Circuit breaker fallback: inventory-service unavailable, cannot release items");
        // Return failed release - this is critical, should be logged and handled manually
        return new InventoryReserveResponse(false, "Inventory service unavailable");
    }
}
