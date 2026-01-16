package org.example.megasegashop.inventory.event;

public record ProductDeletedEvent(
        Long productId,
        String productName
) {
}
