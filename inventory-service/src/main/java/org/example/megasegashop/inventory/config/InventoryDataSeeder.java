package org.example.megasegashop.inventory.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * InventoryDataSeeder is no longer needed.
 * Inventory items are now created automatically via Kafka events
 * when products are created in product-service.
 * 
 * @see org.example.megasegashop.inventory.listener.ProductEventListener
 */
@Component
public class InventoryDataSeeder implements CommandLineRunner {

    @Override
    public void run(String... args) {
        // Inventory items are created via ProductCreatedEvent from Kafka
        // No manual seeding required
    }
}
