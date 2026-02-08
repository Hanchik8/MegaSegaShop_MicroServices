package org.example.megasegashop.notification.service;

import org.example.megasegashop.notification.event.OrderCancelledEvent;
import org.example.megasegashop.notification.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@ConditionalOnBean(KafkaProperties.class)
public class NotificationListener {
    private static final String PLACED_KEY_PREFIX = "notification:processed:order.placed:";
    private static final String CANCELLED_KEY_PREFIX = "notification:processed:order.cancelled:";
    private static final Duration DEDUP_TTL = Duration.ofHours(24);

    private final NotificationDispatcher notificationDispatcher;
    private final StringRedisTemplate redisTemplate;

    public NotificationListener(NotificationDispatcher notificationDispatcher, StringRedisTemplate redisTemplate) {
        this.notificationDispatcher = notificationDispatcher;
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = "order.placed", groupId = "notification-service")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        if (!markProcessed(PLACED_KEY_PREFIX, event.orderId(), "order.placed")) {
            return;
        }
        log.info("Received order.placed event for order {}", event.orderId());
        notificationDispatcher.dispatch(event);
    }

    @KafkaListener(
            topics = "order.cancelled",
            groupId = "notification-service",
            containerFactory = "orderCancelledKafkaListenerContainerFactory"
    )
    public void handleOrderCancelled(OrderCancelledEvent event) {
        if (!markProcessed(CANCELLED_KEY_PREFIX, event.orderId(), "order.cancelled")) {
            return;
        }
        log.info("Received order.cancelled event for order {}", event.orderId());
        notificationDispatcher.dispatchCancellation(event);
    }

    private boolean markProcessed(String prefix, Long orderId, String eventType) {
        if (orderId == null) {
            log.warn("Skipping {} event with missing orderId", eventType);
            return false;
        }

        String dedupKey = prefix + orderId;
        Boolean stored = redisTemplate.opsForValue().setIfAbsent(dedupKey, "1", DEDUP_TTL);
        if (Boolean.TRUE.equals(stored)) {
            return true;
        }

        log.info("Duplicate {} event for order {}, skipping", eventType, orderId);
        return false;
    }
}
