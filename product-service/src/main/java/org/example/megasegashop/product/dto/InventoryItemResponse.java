package org.example.megasegashop.product.dto;

public record InventoryItemResponse(
        Long id,
        Long productId,
        int availableQuantity
) {
}
