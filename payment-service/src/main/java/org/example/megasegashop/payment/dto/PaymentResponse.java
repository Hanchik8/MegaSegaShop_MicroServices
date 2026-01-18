package org.example.megasegashop.payment.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String paymentId,
        boolean approved,
        String status,
        String message,
        BigDecimal amount,
        String currency,
        String riskLevel,
        String cardLast4,
        Instant processedAt
) {
}
