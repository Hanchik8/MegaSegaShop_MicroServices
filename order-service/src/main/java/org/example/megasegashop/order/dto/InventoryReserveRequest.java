package org.example.megasegashop.order.dto;

import java.util.List;

public record InventoryReserveRequest(
        List<InventoryItemRequest> items
) {
}
