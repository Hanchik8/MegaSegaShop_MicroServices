package org.example.megasegashop.inventory.listener;

import org.example.megasegashop.inventory.event.ProductCreatedEvent;
import org.example.megasegashop.inventory.entity.InventoryItem;
import org.example.megasegashop.inventory.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProductEventListener {
    private static final Logger logger = LoggerFactory.getLogger(ProductEventListener.class);

    private final InventoryRepository inventoryRepository;

    public ProductEventListener(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @KafkaListener(topics = "product.created", groupId = "inventory-service")
    @Transactional
    public void handleProductCreated(ProductCreatedEvent event) {
        logger.info("Received ProductCreatedEvent: productId={}, name={}, quantity={}",
                event.productId(), event.productName(), event.initialQuantity());

        // Check if inventory item already exists (idempotency)
        if (inventoryRepository.findByProductId(event.productId()).isPresent()) {
            logger.warn("InventoryItem for productId={} already exists, skipping", event.productId());
            return;
        }

        InventoryItem item = new InventoryItem(
                null,
                event.productId(),
                event.initialQuantity()
        );

        inventoryRepository.save(item);
        logger.info("Created InventoryItem for productId={} with quantity={}",
                event.productId(), event.initialQuantity());
    }
}
