package org.example.megasegashop.product.service.product;

import lombok.RequiredArgsConstructor;
import org.example.megasegashop.product.event.ProductCreatedEvent;
import org.example.megasegashop.product.exceptions.ProductNotFoundException;
import org.example.megasegashop.product.model.Product;
import org.example.megasegashop.product.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {
    private static final String PRODUCT_CREATED_TOPIC = "product.created";

    private final ProductRepository productRepository;
    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;

    @Override
    @CacheEvict(cacheNames = {"products", "product-by-id"}, allEntries = true)
    public Product addProduct(Product product) {
        Product saved = productRepository.save(product);
        
        ProductCreatedEvent event = new ProductCreatedEvent(
                saved.getId(),
                saved.getName(),
                saved.getInventory()
        );
        kafkaTemplate.send(PRODUCT_CREATED_TOPIC, event);
        
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
        productRepository.findById(id).ifPresentOrElse(
                productRepository::delete,
                () -> {
                    throw new ProductNotFoundException("Product not found...");
                }
        );
    }

    @Override
    @CacheEvict(cacheNames = {"products", "product-by-id"}, allEntries = true)
    public void updateProduct(Product product) {
        productRepository.save(product);
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
