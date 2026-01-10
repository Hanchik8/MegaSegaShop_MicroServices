package org.example.megasegashop.notification.event;

import java.math.BigDecimal;

public record OrderPlacedEvent(
        Long orderId,
        String email,
        BigDecimal totalAmount
) {
}
