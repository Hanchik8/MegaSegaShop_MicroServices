package org.example.megasegashop.cart.dto;

import jakarta.validation.constraints.NotNull;

public record RemoveCartItemRequest(
        @NotNull Long userId,
        @NotNull Long productId
) {
}
