package org.example.megasegashop.product.event;

public record ProductCreatedEvent(
        Long productId,
        String productName,
        int initialQuantity
) {
}
