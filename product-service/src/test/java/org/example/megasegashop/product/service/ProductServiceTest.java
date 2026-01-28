package org.example.megasegashop.product.service;

import org.example.megasegashop.product.event.ProductCreatedEvent;
import org.example.megasegashop.product.event.ProductDeletedEvent;
import org.example.megasegashop.product.event.ProductUpdatedEvent;
import org.example.megasegashop.product.exceptions.ProductNotFoundException;
import org.example.megasegashop.product.model.Category;
import org.example.megasegashop.product.model.Product;
import org.example.megasegashop.product.repository.CategoryRepository;
import org.example.megasegashop.product.repository.ProductRepository;
import org.example.megasegashop.product.service.product.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "spring.cache.type=none",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @MockBean
    private KafkaTemplate<String, ProductCreatedEvent> productCreatedKafkaTemplate;

    @MockBean
    private KafkaTemplate<String, ProductUpdatedEvent> productUpdatedKafkaTemplate;

    @MockBean
    private KafkaTemplate<String, ProductDeletedEvent> productDeletedKafkaTemplate;

    @BeforeEach
    void setUp() {
        productRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
    }

    @Test
    void addProduct_success_sendsKafkaEvent() {
        // Given
        Category category = categoryRepository.save(new Category(null, "TestCategory", null));
        Product product = new Product(null, "Test Product", "TestBrand", "Description",
                new BigDecimal("99.99"), category, new ArrayList<>());

        // When
        Product saved = productService.addProduct(product, 10);

        // Then
        assertNotNull(saved.getId());
        assertEquals("Test Product", saved.getName());

        verify(productCreatedKafkaTemplate).send(eq("product.created"), any(ProductCreatedEvent.class));
    }

    @Test
    void getProductById_notFound_throwsException() {
        // Given
        Long nonExistentId = 999L;

        // When/Then
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProductById(nonExistentId)
        );
        assertTrue(exception.getMessage().contains("Product not found"));
    }

    @Test
    void getAllProducts_returnsAllProducts() {
        // Given
        Category category = categoryRepository.save(new Category(null, "Category", null));
        productRepository.save(new Product(null, "Product1", "Brand", "Desc", new BigDecimal("10"), category, new ArrayList<>()));
        productRepository.save(new Product(null, "Product2", "Brand", "Desc", new BigDecimal("20"), category, new ArrayList<>()));

        // When
        var products = productService.getAllProducts();

        // Then
        assertEquals(2, products.size());
    }
}
