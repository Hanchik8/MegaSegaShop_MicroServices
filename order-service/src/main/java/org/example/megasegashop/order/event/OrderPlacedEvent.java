package org.example.megasegashop.order.event;

import java.math.BigDecimal;

public record OrderPlacedEvent(
        Long orderId,
        String email,
        String phone,
        BigDecimal totalAmount
) {
}
