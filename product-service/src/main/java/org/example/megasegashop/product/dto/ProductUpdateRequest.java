package org.example.megasegashop.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProductUpdateRequest(
        @NotBlank String name,
        @NotBlank String brand,
        @NotBlank String description,
        @NotNull @Positive BigDecimal price,
        @NotBlank String category,
        Integer inventoryDelta
) {
}
