package org.example.megasegashop.product.dto;

public record ProductStockResponse(
        Long productId,
        int availableQuantity
) {
}
