package org.example.megasegashop.order.event;

import java.math.BigDecimal;

public record OrderCancelledEvent(
        Long orderId,
        Long userId,
        String email,
        String phone,
        BigDecimal totalAmount
) {
}
