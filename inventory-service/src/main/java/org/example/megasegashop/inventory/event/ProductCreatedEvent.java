package org.example.megasegashop.inventory.event;

public record ProductCreatedEvent(
        Long productId,
        String productName,
        int initialQuantity
) {
}
