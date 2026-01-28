package org.example.megasegashop.notification.service;

import org.example.megasegashop.notification.config.NotificationProperties;
import org.example.megasegashop.notification.event.OrderCancelledEvent;
import org.example.megasegashop.notification.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.net.URI;
import java.util.Locale;

@Slf4j
@Component
public class NotificationDispatcher {
    private final JavaMailSender mailSender;
    private final RestClient restClient;
    private final NotificationProperties properties;

    public NotificationDispatcher(
            JavaMailSender mailSender,
            RestClient.Builder restClientBuilder,
            NotificationProperties properties
    ) {
        this.mailSender = mailSender;
        this.restClient = restClientBuilder.build();
        this.properties = properties;
    }

    public void dispatch(OrderPlacedEvent event) {
        boolean anyEnabled = properties.getEmail().isEnabled()
                || properties.getWebhook().isEnabled()
                || properties.getSms().isEnabled();

        if (!anyEnabled) {
            log.info("Notification channels are disabled; event processed without sending");
            return;
        }

        if (properties.getEmail().isEnabled()) {
            sendEmail(event);
        }
        if (properties.getWebhook().isEnabled()) {
            sendWebhook(event);
        }
        if (properties.getSms().isEnabled()) {
            sendSms(event);
        }
    }

    public void dispatchCancellation(OrderCancelledEvent event) {
        boolean anyEnabled = properties.getEmail().isEnabled()
                || properties.getWebhook().isEnabled()
                || properties.getSms().isEnabled();

        if (!anyEnabled) {
            log.info("Notification channels are disabled; cancellation event processed without sending");
            return;
        }

        if (properties.getEmail().isEnabled()) {
            sendCancellationEmail(event);
        }
        if (properties.getWebhook().isEnabled()) {
            sendCancellationWebhook(event);
        }
        if (properties.getSms().isEnabled()) {
            sendCancellationSms(event);
        }
    }

    private void sendEmail(OrderPlacedEvent event) {
        String from = properties.getEmail().getFrom();
        if (!StringUtils.hasText(from)) {
            log.warn("Email notifications are enabled, but notification.email.from is empty");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(event.email());
            message.setFrom(from);
            message.setSubject(String.format(properties.getEmail().getSubjectTemplate(), event.orderId()));
            message.setText(buildEmailBody(event));
            mailSender.send(message);
            log.info("Email notification sent to {}", event.email());
        } catch (Exception ex) {
            log.error("Failed to send email notification: {}", ex.getMessage());
        }
    }

    private void sendWebhook(OrderPlacedEvent event) {
        String url = properties.getWebhook().getUrl();
        if (!StringUtils.hasText(url)) {
            log.warn("Webhook notifications are enabled, but notification.webhook.url is empty");
            return;
        }

        try {
            if (isDiscordWebhook(url)) {
                postWebhook(url, new DiscordWebhookPayload(buildDiscordContent(event)));
            } else {
                WebhookPayload payload = new WebhookPayload(
                        "order.placed",
                        event.orderId(),
                        event.email(),
                        event.totalAmount(),
                        Instant.now().toString()
                );
                postWebhook(url, payload);
            }
            log.info("Webhook notification sent to {}", redactUrl(url));
        } catch (Exception ex) {
            log.error("Failed to send webhook notification: {}", ex.getMessage());
        }
    }

    private void sendSms(OrderPlacedEvent event) {
        NotificationProperties.Sms sms = properties.getSms();
        if (!StringUtils.hasText(sms.getUrl())) {
            log.warn("SMS notifications are enabled, but notification.sms.url is empty");
            return;
        }

        String recipient = null;
        if (StringUtils.hasText(event.phone())) {
            recipient = event.phone();
        } else if (StringUtils.hasText(sms.getDefaultRecipient())) {
            recipient = sms.getDefaultRecipient();
        }
        if (!StringUtils.hasText(recipient)) {
            log.warn("SMS notifications are enabled, but no recipient is available");
            return;
        }

        try {
            SmsPayload payload = new SmsPayload(
                    recipient,
                    sms.getFrom(),
                    buildSmsBody(event)
            );

            RestClient.RequestHeadersSpec<?> request = restClient.post()
                    .uri(sms.getUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload);

            if (StringUtils.hasText(sms.getApiKey())) {
                String headerValue = sms.getApiKey();
                if (StringUtils.hasText(sms.getApiKeyPrefix())) {
                    headerValue = sms.getApiKeyPrefix() + " " + sms.getApiKey();
                }
                request.header(sms.getApiKeyHeader(), headerValue);
            }

            request.retrieve().toBodilessEntity();
            log.info("SMS notification sent to {}", recipient);
        } catch (Exception ex) {
            log.error("Failed to send SMS notification: {}", ex.getMessage());
        }
    }

    private void sendCancellationEmail(OrderCancelledEvent event) {
        String from = properties.getEmail().getFrom();
        if (!StringUtils.hasText(from)) {
            log.warn("Email notifications are enabled, but notification.email.from is empty");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(event.email());
            message.setFrom(from);
            message.setSubject(String.format("Your MegaSega order %s has been cancelled", event.orderId()));
            message.setText(buildCancellationEmailBody(event));
            mailSender.send(message);
            log.info("Cancellation email sent to {}", event.email());
        } catch (Exception ex) {
            log.error("Failed to send cancellation email: {}", ex.getMessage());
        }
    }

    private void sendCancellationWebhook(OrderCancelledEvent event) {
        String url = properties.getWebhook().getUrl();
        if (!StringUtils.hasText(url)) {
            log.warn("Webhook notifications are enabled, but notification.webhook.url is empty");
            return;
        }

        try {
            if (isDiscordWebhook(url)) {
                postWebhook(url, new DiscordWebhookPayload(buildDiscordContent(event)));
            } else {
                WebhookPayload payload = new WebhookPayload(
                        "order.cancelled",
                        event.orderId(),
                        event.email(),
                        event.totalAmount(),
                        Instant.now().toString()
                );
                postWebhook(url, payload);
            }
            log.info("Cancellation webhook sent to {}", redactUrl(url));
        } catch (Exception ex) {
            log.error("Failed to send cancellation webhook: {}", ex.getMessage());
        }
    }

    private void sendCancellationSms(OrderCancelledEvent event) {
        NotificationProperties.Sms sms = properties.getSms();
        if (!StringUtils.hasText(sms.getUrl())) {
            log.warn("SMS notifications are enabled, but notification.sms.url is empty");
            return;
        }

        String recipient = null;
        if (StringUtils.hasText(event.phone())) {
            recipient = event.phone();
        } else if (StringUtils.hasText(sms.getDefaultRecipient())) {
            recipient = sms.getDefaultRecipient();
        }
        if (!StringUtils.hasText(recipient)) {
            log.warn("SMS notifications are enabled, but no recipient is available");
            return;
        }

        try {
            SmsPayload payload = new SmsPayload(
                    recipient,
                    sms.getFrom(),
                    buildCancellationSmsBody(event)
            );

            RestClient.RequestHeadersSpec<?> request = restClient.post()
                    .uri(sms.getUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload);

            if (StringUtils.hasText(sms.getApiKey())) {
                String headerValue = sms.getApiKey();
                if (StringUtils.hasText(sms.getApiKeyPrefix())) {
                    headerValue = sms.getApiKeyPrefix() + " " + sms.getApiKey();
                }
                request.header(sms.getApiKeyHeader(), headerValue);
            }

            request.retrieve().toBodilessEntity();
            log.info("Cancellation SMS sent to {}", recipient);
        } catch (Exception ex) {
            log.error("Failed to send cancellation SMS: {}", ex.getMessage());
        }
    }

    private String buildEmailBody(OrderPlacedEvent event) {
        return "Thanks for your order!\n"
                + "Order ID: " + event.orderId() + "\n"
                + "Total: " + formatAmount(event.totalAmount()) + "\n";
    }

    private String buildCancellationEmailBody(OrderCancelledEvent event) {
        return "Your order has been cancelled.\n"
                + "Order ID: " + event.orderId() + "\n"
                + "Total: " + formatAmount(event.totalAmount()) + "\n";
    }

    private String buildSmsBody(OrderPlacedEvent event) {
        return "MegaSega order " + event.orderId()
                + " placed. Total: " + formatAmount(event.totalAmount());
    }

    private String buildCancellationSmsBody(OrderCancelledEvent event) {
        return "MegaSega order " + event.orderId()
                + " cancelled. Total: " + formatAmount(event.totalAmount());
    }

    private String formatAmount(BigDecimal amount) {
        return amount != null ? amount.toPlainString() : "0";
    }

    private void postWebhook(String url, Object payload) {
        RestClient.RequestHeadersSpec<?> request = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload);

        if (StringUtils.hasText(properties.getWebhook().getSecret())) {
            request.header("X-Webhook-Secret", properties.getWebhook().getSecret());
        }

        request.retrieve().toBodilessEntity();
    }

    private boolean isDiscordWebhook(String url) {
        String lower = url.toLowerCase(Locale.ROOT);
        return lower.contains("discord.com/api/webhooks")
                || lower.contains("discordapp.com/api/webhooks");
    }

    private String buildDiscordContent(OrderPlacedEvent event) {
        return buildDiscordContent(
                "Order placed",
                event.orderId(),
                null,
                event.email(),
                event.phone(),
                event.totalAmount()
        );
    }

    private String buildDiscordContent(OrderCancelledEvent event) {
        return buildDiscordContent(
                "Order cancelled",
                event.orderId(),
                event.userId(),
                event.email(),
                event.phone(),
                event.totalAmount()
        );
    }

    private String buildDiscordContent(
            String header,
            Long orderId,
            Long userId,
            String email,
            String phone,
            BigDecimal totalAmount
    ) {
        StringBuilder builder = new StringBuilder();
        builder.append(header).append('\n')
                .append("Order ID: ").append(valueOrDash(orderId)).append('\n');
        if (userId != null) {
            builder.append("User ID: ").append(userId).append('\n');
        }
        builder.append("Email: ").append(valueOrDash(email)).append('\n')
                .append("Phone: ").append(valueOrDash(phone)).append('\n')
                .append("Total: ").append(formatAmount(totalAmount));
        return builder.toString();
    }

    private String valueOrDash(String value) {
        return StringUtils.hasText(value) ? value : "-";
    }

    private String valueOrDash(Long value) {
        return value != null ? value.toString() : "-";
    }

    private String redactUrl(String url) {
        try {
            URI uri = URI.create(url);
            if (!StringUtils.hasText(uri.getHost())) {
                return "<redacted>";
            }
            String scheme = StringUtils.hasText(uri.getScheme()) ? uri.getScheme() + "://" : "";
            return scheme + uri.getHost();
        } catch (Exception ex) {
            return "<redacted>";
        }
    }

    private record WebhookPayload(
            String type,
            Long orderId,
            String email,
            BigDecimal totalAmount,
            String occurredAt
    ) {
    }

    private record DiscordWebhookPayload(
            String content
    ) {
    }

    private record SmsPayload(
            String to,
            String from,
            String message
    ) {
    }
}
