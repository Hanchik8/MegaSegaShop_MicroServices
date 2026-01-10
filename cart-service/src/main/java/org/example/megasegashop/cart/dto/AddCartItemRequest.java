package org.example.megasegashop.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request to add item to cart.
 * Product name and price are fetched from product-service automatically.
 */
public record AddCartItemRequest(
        @NotNull Long userId,
        @NotNull Long productId,
        @NotNull @Min(1) Integer quantity
) {
}
