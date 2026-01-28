package org.example.megasegashop.order.client;

import org.example.megasegashop.order.dto.CartSnapshot;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cart-service", fallback = CartClientFallback.class)
public interface CartClient {
    @GetMapping("/cart/{userId}")
    CartSnapshot getCart(@PathVariable("userId") Long userId);

    @DeleteMapping("/cart/{userId}")
    void clearCart(@PathVariable("userId") Long userId);
}
