package org.example.megasegashop.payment.controller;

import jakarta.validation.Valid;
import org.example.megasegashop.payment.dto.PaymentRequest;
import org.example.megasegashop.payment.dto.PaymentResponse;
import org.example.megasegashop.payment.service.PaymentProcessor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentProcessor paymentProcessor;

    public PaymentController(PaymentProcessor paymentProcessor) {
        this.paymentProcessor = paymentProcessor;
    }

    @PostMapping("/charge")
    public PaymentResponse charge(@Valid @RequestBody PaymentRequest request) {
        return paymentProcessor.charge(request);
    }
}
