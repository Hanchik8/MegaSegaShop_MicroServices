package org.example.megasegashop.order.client;

import org.example.megasegashop.shared.dto.InventoryReserveRequest;
import org.example.megasegashop.shared.dto.InventoryReserveResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "inventory-service",
        fallback = InventoryClientFallback.class,
        configuration = InventoryClientConfig.class
)
public interface InventoryClient {
    @PostMapping("/inventory/reserve")
    InventoryReserveResponse reserve(@RequestBody InventoryReserveRequest request);

    /**
     * Compensating action: releases previously reserved items.
     * Used by Saga pattern when order creation fails.
     */
    @PostMapping("/inventory/release")
    InventoryReserveResponse release(@RequestBody InventoryReserveRequest request);
}
