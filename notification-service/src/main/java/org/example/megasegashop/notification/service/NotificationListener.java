package org.example.megasegashop.notification.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.example.megasegashop.notification.event.OrderCancelledEvent;
import org.example.megasegashop.notification.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class NotificationListener {
    private final Cache<Long, Boolean> processedPlacedOrders = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofHours(24))
            .build();
    private final Cache<Long, Boolean> processedCancelledOrders = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofHours(24))
            .build();
    private final NotificationDispatcher notificationDispatcher;

    public NotificationListener(NotificationDispatcher notificationDispatcher) {
        this.notificationDispatcher = notificationDispatcher;
    }

    @KafkaListener(topics = "order.placed", groupId = "notification-service")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        if (!markProcessed(processedPlacedOrders, event.orderId(), "order.placed")) {
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
        if (!markProcessed(processedCancelledOrders, event.orderId(), "order.cancelled")) {
            return;
        }
        log.info("Received order.cancelled event for order {}", event.orderId());
        notificationDispatcher.dispatchCancellation(event);
    }

    private boolean markProcessed(Cache<Long, Boolean> cache, Long orderId, String eventType) {
        if (orderId == null) {
            log.warn("Skipping {} event with missing orderId", eventType);
            return false;
        }

        if (cache.getIfPresent(orderId) != null) {
            log.info("Duplicate {} event for order {}, skipping", eventType, orderId);
            return false;
        }

        cache.put(orderId, Boolean.TRUE);
        return true;
    }
}
