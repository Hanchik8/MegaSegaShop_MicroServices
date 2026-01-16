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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private static final String PRODUCT_CREATED_TOPIC = "product.created";
    private static final String PRODUCT_UPDATED_TOPIC = "product.updated";
    private static final String PRODUCT_DELETED_TOPIC = "product.deleted";

    private final ProductRepository productRepository;
    private final KafkaTemplate<String, ProductCreatedEvent> productCreatedKafkaTemplate;
    private final KafkaTemplate<String, ProductUpdatedEvent> productUpdatedKafkaTemplate;
    private final KafkaTemplate<String, ProductDeletedEvent> productDeletedKafkaTemplate;

    @Override
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

    @Override
    @Cacheable(cacheNames = "products")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    @Cacheable(cacheNames = "product-by-id", key = "#id")
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found..."));
    }

    @Override
    @CacheEvict(cacheNames = {"products", "product-by-id"}, allEntries = true)
    public void deleteProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found..."));

        productRepository.delete(product);

        ProductDeletedEvent event = new ProductDeletedEvent(product.getId(), product.getName());
        productDeletedKafkaTemplate.send(PRODUCT_DELETED_TOPIC, event);
        logger.info("Product deleted: id={}, name={}", product.getId(), product.getName());
    }

    @Override
    @CacheEvict(cacheNames = {"products", "product-by-id"}, allEntries = true)
    public Product updateProduct(Long id, ProductUpdateRequest request, Category category) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found..."));

        boolean changed = false;

        if (!existing.getName().equals(request.name())) {
            existing.setName(request.name());
            changed = true;
        }
        if (!existing.getBrand().equals(request.brand())) {
            existing.setBrand(request.brand());
            changed = true;
        }
        if (!existing.getDescription().equals(request.description())) {
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

    @Override
    public List<Product> getProductsByPriceRange(BigDecimal min, BigDecimal max) {
        return productRepository.findByPriceBetween(min, max);
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryName(category);
    }

    @Override
    public List<Product> getProductsByName(String name) {
        return productRepository.findByName(name);
    }


    @Override
    public List<Product> getProductsByPrice(BigDecimal price) {
        return productRepository.findByPrice(price);
    }

    @Override
    public List<Product> getProductsByBrand(String brand) {
        return productRepository.findByBrand(brand);
    }

    @Override
    public List<Product> getProductsByCategoryAndPrice(String category, BigDecimal price) {
        return productRepository.findByCategoryNameAndPrice(category, price);
    }

    @Override
    public List<Product> getProductsByCategoryAndBrand(String category, String brand) {
        return productRepository.findByCategoryNameAndBrand(category, brand);
    }

    @Override
    public List<Product> getProductsByCategoryAndPriceAndName(String category, BigDecimal price, String name) {
        return productRepository.findByCategoryNameAndPriceAndName(category, price, name);
    }

    @Override
    public List<Product> getProductsByBrandAndName(String brand, String name) {
        return productRepository.findByBrandAndName(brand, name);
    }
}
