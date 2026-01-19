package org.example.megasegashop.inventory.service;

import org.example.megasegashop.shared.dto.InventoryItemRequest;
import org.example.megasegashop.shared.dto.InventoryReserveRequest;
import org.example.megasegashop.shared.dto.InventoryReserveResponse;
import org.example.megasegashop.inventory.entity.InventoryItem;
import org.example.megasegashop.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public InventoryItem getByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId).orElse(null);
    }

    @Transactional
    public InventoryReserveResponse reserveItems(InventoryReserveRequest request) {
        List<InventoryItem> lockedItems = new ArrayList<>();

        for (InventoryItemRequest itemRequest : request.items()) {
            InventoryItem item = inventoryRepository.findWithLockByProductId(itemRequest.productId())
                    .orElse(null);
            if (item == null) {
                return new InventoryReserveResponse(false, "Inventory item missing for productId: " + itemRequest.productId());
            }
            if (item.getAvailableQuantity() < itemRequest.quantity()) {
                return new InventoryReserveResponse(false, "Insufficient stock for productId: " + itemRequest.productId());
            }
            lockedItems.add(item);
        }

        for (int i = 0; i < request.items().size(); i++) {
            InventoryItemRequest itemRequest = request.items().get(i);
            InventoryItem item = lockedItems.get(i);
            item.setAvailableQuantity(item.getAvailableQuantity() - itemRequest.quantity());
            inventoryRepository.save(item);
        }

        return new InventoryReserveResponse(true, "Reserved");
    }

    /**
     * Compensating transaction: releases previously reserved items.
     * Used by Saga pattern when order creation fails after reservation.
     */
    @Transactional
    public InventoryReserveResponse releaseItems(InventoryReserveRequest request) {
        for (InventoryItemRequest itemRequest : request.items()) {
            InventoryItem item = inventoryRepository.findWithLockByProductId(itemRequest.productId())
                    .orElse(null);
            if (item == null) {
                // Item doesn't exist, skip (idempotent behavior)
                continue;
            }
            item.setAvailableQuantity(item.getAvailableQuantity() + itemRequest.quantity());
            inventoryRepository.save(item);
        }

        return new InventoryReserveResponse(true, "Released");
    }
}
