package org.example.megasegashop.payment.service;

import org.example.megasegashop.payment.dto.PaymentRequest;
import org.example.megasegashop.payment.dto.PaymentResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.UUID;

/**
 * Mock payment processor for demonstration purposes.
 * 
 * <p><b>SECURITY WARNING:</b> This service handles sensitive credit card data.
 * In a production environment:
 * <ul>
 *   <li>Never log full card numbers - only last 4 digits</li>
 *   <li>Card data should not be stored persistently</li>
 *   <li>Use tokenization via a PCI-compliant payment gateway (Stripe, Braintree, etc.)</li>
 *   <li>Ensure all card data is transmitted over TLS</li>
 *   <li>Comply with PCI-DSS requirements</li>
 * </ul>
 */
@Service
public class PaymentProcessor {
    public PaymentResponse charge(PaymentRequest request) {
        String normalized = normalizeCardNumber(request.cardNumber());
        boolean luhnValid = LuhnValidator.isValid(normalized);
        boolean expiryValid = isExpiryValid(request.expiry());
        boolean cvcValid = isCvcValid(request.cvc());

        if (!luhnValid || !expiryValid || !cvcValid) {
            return buildResponse(false, "DECLINED", "Payment details invalid.", request, "HIGH", normalized);
        }

        boolean decline = normalized.startsWith("4000");
        if (decline) {
            return buildResponse(false, "DECLINED", "Mock processor decline.", request, "HIGH", normalized);
        }

        String risk = normalized.startsWith("37") || normalized.startsWith("34") ? "ELEVATED" : "LOW";
        return buildResponse(true, "APPROVED", "Payment approved.", request, risk, normalized);
    }

    private PaymentResponse buildResponse(
            boolean approved,
            String status,
            String message,
            PaymentRequest request,
            String risk,
            String normalized
    ) {
        String paymentId = "pay_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return new PaymentResponse(
                paymentId,
                approved,
                status,
                message,
                request.amount(),
                request.currency(),
                risk,
                last4(normalized),
                Instant.now()
        );
    }

    private String normalizeCardNumber(String cardNumber) {
        if (cardNumber == null) {
            return "";
        }
        return cardNumber.replaceAll("\\D", "");
    }

    private String last4(String normalized) {
        if (normalized.length() < 4) {
            return "----";
        }
        return normalized.substring(normalized.length() - 4);
    }

    private boolean isCvcValid(String cvc) {
        if (cvc == null) {
            return false;
        }
        return cvc.matches("\\d{3,4}");
    }

    private boolean isExpiryValid(String expiry) {
        if (expiry == null) {
            return false;
        }
        String trimmed = expiry.trim();
        String[] parts = trimmed.split("/");
        if (parts.length != 2) {
            return false;
        }
        String monthPart = parts[0].trim();
        String yearPart = parts[1].trim();
        if (monthPart.length() != 2) {
            return false;
        }
        int month;
        int year;
        try {
            month = Integer.parseInt(monthPart);
            if (yearPart.length() == 2) {
                year = 2000 + Integer.parseInt(yearPart);
            } else if (yearPart.length() == 4) {
                year = Integer.parseInt(yearPart);
            } else {
                return false;
            }
        } catch (NumberFormatException ex) {
            return false;
        }
        if (month < 1 || month > 12) {
            return false;
        }
        YearMonth expiryMonth = YearMonth.of(year, month);
        return !expiryMonth.isBefore(YearMonth.now());
    }
}
