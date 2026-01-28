package org.example.megasegashop.product.event;

public record ProductUpdatedEvent(
        Long productId,
        String productName,
        int inventoryDelta
) {
}
