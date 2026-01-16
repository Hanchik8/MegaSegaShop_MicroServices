package org.example.megasegashop.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventoryItemRequest(
        @NotNull Long productId,
        @NotNull @Min(1) Integer quantity
) {
}
