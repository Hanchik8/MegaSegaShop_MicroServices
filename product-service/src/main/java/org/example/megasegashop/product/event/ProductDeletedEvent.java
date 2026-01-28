package org.example.megasegashop.product.event;

public record ProductDeletedEvent(
        Long productId,
        String productName
) {
}
