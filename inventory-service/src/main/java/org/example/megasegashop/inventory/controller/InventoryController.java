package org.example.megasegashop.inventory.controller;

import jakarta.validation.Valid;
import org.example.megasegashop.inventory.dto.InventoryReserveRequest;
import org.example.megasegashop.inventory.dto.InventoryReserveResponse;
import org.example.megasegashop.inventory.entity.InventoryItem;
import org.example.megasegashop.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryItem> getByProduct(@PathVariable Long productId) {
        InventoryItem item = inventoryService.getByProductId(productId);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    @PostMapping("/reserve")
    public InventoryReserveResponse reserve(@Valid @RequestBody InventoryReserveRequest request) {
        return inventoryService.reserveItems(request);
    }

    /**
     * Compensating endpoint: releases previously reserved items.
     * Used by Saga pattern when order creation fails after reservation.
     */
    @PostMapping("/release")
    public InventoryReserveResponse release(@Valid @RequestBody InventoryReserveRequest request) {
        return inventoryService.releaseItems(request);
    }
}
