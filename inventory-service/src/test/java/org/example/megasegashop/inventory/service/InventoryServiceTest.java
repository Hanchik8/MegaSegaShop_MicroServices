package org.example.megasegashop.inventory.service;

import org.example.megasegashop.shared.dto.InventoryItemRequest;
import org.example.megasegashop.shared.dto.InventoryReserveRequest;
import org.example.megasegashop.shared.dto.InventoryReserveResponse;
import org.example.megasegashop.inventory.entity.InventoryItem;
import org.example.megasegashop.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InventoryServiceTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @BeforeEach
    void setUp() {
        inventoryRepository.deleteAll();
    }

    @Test
    void reserveItems_withInsufficientStock_returnsFalse() {
        // Given
        Long productId = 1L;
        InventoryItem item = new InventoryItem(null, productId, 5);
        inventoryRepository.save(item);

        InventoryReserveRequest request = new InventoryReserveRequest(
                List.of(new InventoryItemRequest(productId, 10))
        );

        // When
        InventoryReserveResponse response = inventoryService.reserveItems(request);

        // Then
        assertFalse(response.success());
        assertTrue(response.message().contains("Insufficient stock"));
    }

    @Test
    void reserveItems_success_decreasesQuantity() {
        // Given
        Long productId = 2L;
        InventoryItem item = new InventoryItem(null, productId, 20);
        inventoryRepository.save(item);

        InventoryReserveRequest request = new InventoryReserveRequest(
                List.of(new InventoryItemRequest(productId, 5))
        );

        // When
        InventoryReserveResponse response = inventoryService.reserveItems(request);

        // Then
        assertTrue(response.success());
        assertEquals("Reserved", response.message());

        InventoryItem updated = inventoryRepository.findByProductId(productId).orElseThrow();
        assertEquals(15, updated.getAvailableQuantity());
    }

    @Test
    void releaseItems_success_increasesQuantity() {
        // Given
        Long productId = 3L;
        InventoryItem item = new InventoryItem(null, productId, 10);
        inventoryRepository.save(item);

        InventoryReserveRequest request = new InventoryReserveRequest(
                List.of(new InventoryItemRequest(productId, 5))
        );

        // When
        InventoryReserveResponse response = inventoryService.releaseItems(request);

        // Then
        assertTrue(response.success());

        InventoryItem updated = inventoryRepository.findByProductId(productId).orElseThrow();
        assertEquals(15, updated.getAvailableQuantity());
    }
}
