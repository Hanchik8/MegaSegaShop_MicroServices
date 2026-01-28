package org.example.megasegashop.cart.dto;

import java.math.BigDecimal;

/**
 * DTO for product information received from product-service.
 * Used to validate products and get actual prices when adding to cart.
 */
public record ProductResponse(
        Long id,
        String name,
        String brand,
        String description,
        BigDecimal price,
        String category
) {
}
