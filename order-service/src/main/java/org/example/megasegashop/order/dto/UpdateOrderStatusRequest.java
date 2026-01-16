package org.example.megasegashop.order.dto;

import jakarta.validation.constraints.NotNull;
import org.example.megasegashop.order.entity.OrderStatus;

public record UpdateOrderStatusRequest(
        @NotNull OrderStatus status
) {
}
