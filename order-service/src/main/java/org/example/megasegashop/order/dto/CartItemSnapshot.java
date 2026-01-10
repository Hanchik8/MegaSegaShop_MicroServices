package org.example.megasegashop.order.dto;

import java.math.BigDecimal;

public record CartItemSnapshot(
        Long productId,
        String productName,
        BigDecimal unitPrice,
        int quantity
) {
}
