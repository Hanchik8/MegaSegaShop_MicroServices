package org.example.megasegashop.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProductCreateRequest(
        @NotBlank String name,
        @NotBlank String brand,
        @NotBlank String description,
        @NotNull @Positive BigDecimal price,
        @NotNull @Min(0) Integer inventory,
        @NotBlank String category
) {
}
