package org.example.megasegashop.inventory.listener;

import org.example.megasegashop.inventory.event.ProductCreatedEvent;
import org.example.megasegashop.inventory.event.ProductDeletedEvent;
import org.example.megasegashop.inventory.event.ProductUpdatedEvent;
import org.example.megasegashop.inventory.entity.InventoryItem;
import org.example.megasegashop.inventory.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class ProductEventListener {
    private final InventoryRepository inventoryRepository;

    public ProductEventListener(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @KafkaListener(topics = "product.created", groupId = "inventory-service")
    @Transactional
    public void handleProductCreated(ProductCreatedEvent event) {
        log.info("Received ProductCreatedEvent: productId={}, name={}, quantity={}",
                event.productId(), event.productName(), event.initialQuantity());

        // Check if inventory item already exists (idempotency)
        if (inventoryRepository.findByProductId(event.productId()).isPresent()) {
            log.warn("InventoryItem for productId={} already exists, skipping", event.productId());
            return;
        }

        InventoryItem item = new InventoryItem(
                null,
                event.productId(),
                event.initialQuantity()
        );

        inventoryRepository.save(item);
        log.info("Created InventoryItem for productId={} with quantity={}",
                event.productId(), event.initialQuantity());
    }

    @KafkaListener(
            topics = "product.updated",
            groupId = "inventory-service",
            containerFactory = "productUpdatedKafkaListenerContainerFactory"
    )
    @Transactional
    public void handleProductUpdated(ProductUpdatedEvent event) {
        log.info("Received ProductUpdatedEvent: productId={}, name={}, delta={}",
                event.productId(), event.productName(), event.inventoryDelta());

        InventoryItem item = inventoryRepository.findWithLockByProductId(event.productId())
                .orElseGet(() -> new InventoryItem(null, event.productId(), 0));

        int updatedQuantity = item.getAvailableQuantity() + event.inventoryDelta();
        if (updatedQuantity < 0) {
            log.warn("Inventory delta would make quantity negative for productId={}, clamping to 0",
                    event.productId());
            updatedQuantity = 0;
        }
        item.setAvailableQuantity(updatedQuantity);
        inventoryRepository.save(item);
        log.info("Inventory updated for productId={} to quantity={}",
                event.productId(), updatedQuantity);
    }

    @KafkaListener(
            topics = "product.deleted",
            groupId = "inventory-service",
            containerFactory = "productDeletedKafkaListenerContainerFactory"
    )
    @Transactional
    public void handleProductDeleted(ProductDeletedEvent event) {
        log.info("Received ProductDeletedEvent: productId={}, name={}",
                event.productId(), event.productName());

        if (event.productId() == null) {
            return;
        }

        if (inventoryRepository.findByProductId(event.productId()).isEmpty()) {
            log.info("Inventory item already removed for productId={}", event.productId());
            return;
        }

        inventoryRepository.deleteByProductId(event.productId());
        log.info("Inventory item deleted for productId={}", event.productId());
    }
}
