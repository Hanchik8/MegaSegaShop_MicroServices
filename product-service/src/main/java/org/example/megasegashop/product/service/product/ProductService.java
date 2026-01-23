package org.example.megasegashop.product.service.product;

import lombok.RequiredArgsConstructor;
import org.example.megasegashop.product.event.ProductDeletedEvent;
import org.example.megasegashop.product.dto.ProductUpdateRequest;
import org.example.megasegashop.product.event.ProductCreatedEvent;
import org.example.megasegashop.product.event.ProductUpdatedEvent;
import org.example.megasegashop.product.exceptions.ProductNotFoundException;
import org.example.megasegashop.product.model.Category;
import org.example.megasegashop.product.model.Product;
import org.example.megasegashop.product.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private static final String PRODUCT_CREATED_TOPIC = "product.created";
    private static final String PRODUCT_UPDATED_TOPIC = "product.updated";
    private static final String PRODUCT_DELETED_TOPIC = "product.deleted";

    private final ProductRepository productRepository;
    private final KafkaTemplate<String, ProductCreatedEvent> productCreatedKafkaTemplate;
    private final KafkaTemplate<String, ProductUpdatedEvent> productUpdatedKafkaTemplate;
    private final KafkaTemplate<String, ProductDeletedEvent> productDeletedKafkaTemplate;

    @Transactional
    @CacheEvict(cacheNames = {"products", "product-by-id"}, allEntries = true)
    public Product addProduct(Product product, int initialQuantity) {
        Product saved = productRepository.save(product);
        
        ProductCreatedEvent event = new ProductCreatedEvent(
                saved.getId(),
                saved.getName(),
                initialQuantity
        );
        productCreatedKafkaTemplate.send(PRODUCT_CREATED_TOPIC, event);
        
        return saved;
    }

    @Cacheable(cacheNames = "products")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Cacheable(cacheNames = "product-by-id", key = "#id")
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found..."));
    }

    @Transactional
    @CacheEvict(cacheNames = {"products", "product-by-id"}, allEntries = true)
    public void deleteProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found..."));

        productRepository.delete(product);

        ProductDeletedEvent event = new ProductDeletedEvent(product.getId(), product.getName());
        productDeletedKafkaTemplate.send(PRODUCT_DELETED_TOPIC, event);
        log.info("Product deleted: id={}, name={}", product.getId(), product.getName());
    }

    @Transactional
    @CacheEvict(cacheNames = {"products", "product-by-id"}, allEntries = true)
    public Product updateProduct(Long id, ProductUpdateRequest request, Category category) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found..."));

        boolean changed = false;

        if (!Objects.equals(existing.getName(), request.name())) {
            existing.setName(request.name());
            changed = true;
        }
        if (!Objects.equals(existing.getBrand(), request.brand())) {
            existing.setBrand(request.brand());
            changed = true;
        }
        if (!Objects.equals(existing.getDescription(), request.description())) {
            existing.setDescription(request.description());
            changed = true;
        }
        if (existing.getPrice() == null || existing.getPrice().compareTo(request.price()) != 0) {
            existing.setPrice(request.price());
            changed = true;
        }
        if (category != null) {
            if (existing.getCategory() == null
                    || !existing.getCategory().getId().equals(category.getId())) {
                existing.setCategory(category);
                changed = true;
            }
        }

        int inventoryDelta = request.inventoryDelta() != null ? request.inventoryDelta() : 0;
        Product saved = existing;
        if (changed) {
            saved = productRepository.save(existing);
        }

        if (changed || inventoryDelta != 0) {
            ProductUpdatedEvent event = new ProductUpdatedEvent(
                    saved.getId(),
                    saved.getName(),
                    inventoryDelta
            );
            productUpdatedKafkaTemplate.send(PRODUCT_UPDATED_TOPIC, event);
        }

        return saved;
    }

    public List<Product> getProductsByPriceRange(BigDecimal min, BigDecimal max) {
        return productRepository.findByPriceBetween(min, max);
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryName(category);
    }

    public List<Product> getProductsByName(String name) {
        return productRepository.findByName(name);
    }

    public List<Product> getProductsByPrice(BigDecimal price) {
        return productRepository.findByPrice(price);
    }

    public List<Product> getProductsByBrand(String brand) {
        return productRepository.findByBrand(brand);
    }

    public List<Product> getProductsByCategoryAndPrice(String category, BigDecimal price) {
        return productRepository.findByCategoryNameAndPrice(category, price);
    }

    public List<Product> getProductsByCategoryAndBrand(String category, String brand) {
        return productRepository.findByCategoryNameAndBrand(category, brand);
    }

    public List<Product> getProductsByCategoryAndPriceAndName(String category, BigDecimal price, String name) {
        return productRepository.findByCategoryNameAndPriceAndName(category, price, name);
    }

    public List<Product> getProductsByBrandAndName(String brand, String name) {
        return productRepository.findByBrandAndName(brand, name);
    }
}
