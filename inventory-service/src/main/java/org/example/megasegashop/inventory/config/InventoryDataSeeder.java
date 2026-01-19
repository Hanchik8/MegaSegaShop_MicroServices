package org.example.megasegashop.inventory.config;

import org.example.megasegashop.inventory.client.ProductCatalogClient;
import org.example.megasegashop.inventory.dto.ProductSnapshot;
import org.example.megasegashop.inventory.entity.InventoryItem;
import org.example.megasegashop.inventory.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "inventory.seed", name = "enabled", havingValue = "true")
public class InventoryDataSeeder implements CommandLineRunner {
    private final ProductCatalogClient productCatalogClient;
    private final InventoryRepository inventoryRepository;
    private final InventorySeedProperties seedProperties;

    public InventoryDataSeeder(
            ProductCatalogClient productCatalogClient,
            InventoryRepository inventoryRepository,
            InventorySeedProperties seedProperties
    ) {
        this.productCatalogClient = productCatalogClient;
        this.inventoryRepository = inventoryRepository;
        this.seedProperties = seedProperties;
    }

    @Override
    public void run(String... args) {
        List<ProductSnapshot> products;
        try {
            products = productCatalogClient.fetchProducts();
        } catch (Exception ex) {
            log.warn("Inventory seeding skipped: failed to fetch products: {}", ex.getMessage());
            return;
        }

        if (products.isEmpty()) {
            log.info("Inventory seeding skipped: product list is empty");
            return;
        }

        int created = 0;
        int defaultQuantity = Math.max(0, seedProperties.getDefaultQuantity());
        for (ProductSnapshot product : products) {
            if (product.id() == null) {
                continue;
            }
            if (inventoryRepository.findByProductId(product.id()).isPresent()) {
                continue;
            }
            InventoryItem item = new InventoryItem(null, product.id(), defaultQuantity);
            inventoryRepository.save(item);
            created++;
        }
        log.info("Inventory seeding completed: created {} item(s)", created);
    }
}
