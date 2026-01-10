package org.example.megasegashop.order.dto;

public record InventoryItemRequest(
        Long productId,
        int quantity
) {
}
