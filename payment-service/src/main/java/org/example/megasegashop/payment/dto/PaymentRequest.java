package org.example.megasegashop.payment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull @Positive BigDecimal amount,
        @NotBlank String currency,
        @NotBlank String cardNumber,
        @NotBlank String cardHolder,
        @NotBlank String expiry,
        @NotBlank String cvc,
        @Email String email,
        String reference
) {
}
