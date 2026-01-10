package org.example.megasegashop.order.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartSnapshot(
        Long userId,
        List<CartItemSnapshot> items,
        BigDecimal totalAmount
) {
}
