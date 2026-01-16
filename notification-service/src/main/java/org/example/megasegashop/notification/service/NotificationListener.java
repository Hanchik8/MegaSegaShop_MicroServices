package org.example.megasegashop.notification.service;

import org.example.megasegashop.notification.event.OrderCancelledEvent;
import org.example.megasegashop.notification.event.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class NotificationListener {
    private static final Logger logger = LoggerFactory.getLogger(NotificationListener.class);
    private static final int MAX_DEDUPED_ORDERS = 10000;

    private final ConcurrentMap<Long, Boolean> processedPlacedOrders = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Boolean> processedCancelledOrders = new ConcurrentHashMap<>();
    private final NotificationDispatcher notificationDispatcher;

    public NotificationListener(NotificationDispatcher notificationDispatcher) {
        this.notificationDispatcher = notificationDispatcher;
    }

    @KafkaListener(topics = "order.placed", groupId = "notification-service")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        if (!markProcessed(processedPlacedOrders, event.orderId(), "order.placed")) {
            return;
        }
        logger.info("Received order.placed event for order {}", event.orderId());
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
        logger.info("Received order.cancelled event for order {}", event.orderId());
        notificationDispatcher.dispatchCancellation(event);
    }

    private boolean markProcessed(ConcurrentMap<Long, Boolean> cache, Long orderId, String eventType) {
        if (orderId == null) {
            logger.warn("Skipping {} event with missing orderId", eventType);
            return false;
        }

        if (cache.putIfAbsent(orderId, Boolean.TRUE) != null) {
            logger.info("Duplicate {} event for order {}, skipping", eventType, orderId);
            return false;
        }

        if (cache.size() > MAX_DEDUPED_ORDERS) {
            cache.clear();
        }

        return true;
    }
}
