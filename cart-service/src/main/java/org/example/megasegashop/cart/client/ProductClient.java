package org.example.megasegashop.cart.client;

import org.example.megasegashop.cart.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", fallback = ProductClientFallback.class)
public interface ProductClient {
    
    @GetMapping("/products/{id}")
    ProductResponse getProduct(@PathVariable("id") Long id);
}
