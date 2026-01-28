package org.example.megasegashop.inventory.event;

public record ProductUpdatedEvent(
        Long productId,
        String productName,
        int inventoryDelta
) {
}
