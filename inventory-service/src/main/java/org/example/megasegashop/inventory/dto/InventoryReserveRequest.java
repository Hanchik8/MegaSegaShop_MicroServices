package org.example.megasegashop.inventory.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record InventoryReserveRequest(
        @NotEmpty List<InventoryItemRequest> items
) {
}
