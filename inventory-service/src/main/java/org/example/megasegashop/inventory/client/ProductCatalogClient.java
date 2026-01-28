package org.example.megasegashop.inventory.client;

import org.example.megasegashop.inventory.dto.ProductSnapshot;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Component
public class ProductCatalogClient {
    private static final ParameterizedTypeReference<List<ProductSnapshot>> LIST_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient restClient;

    public ProductCatalogClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<ProductSnapshot> fetchProducts() {
        List<ProductSnapshot> products = restClient.get()
                .uri("/products")
                .retrieve()
                .body(LIST_TYPE);
        return products != null ? products : Collections.emptyList();
    }
}
