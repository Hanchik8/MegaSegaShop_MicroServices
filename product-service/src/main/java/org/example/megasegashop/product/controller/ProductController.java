package org.example.megasegashop.product.controller;

import jakarta.validation.Valid;
import org.example.megasegashop.product.dto.ProductCreateRequest;
import org.example.megasegashop.product.dto.ProductResponse;
import org.example.megasegashop.product.model.Category;
import org.example.megasegashop.product.model.Product;
import org.example.megasegashop.product.repository.CategoryRepository;
import org.example.megasegashop.product.service.product.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    public ProductController(ProductService productService, CategoryRepository categoryRepository) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public List<ProductResponse> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand
    ) {
        List<Product> products;
        if (category != null && brand != null) {
            products = productService.getProductsByCategoryAndBrand(category, brand);
        } else if (category != null) {
            products = productService.getProductsByCategory(category);
        } else if (brand != null) {
            products = productService.getProductsByBrand(brand);
        } else {
            products = productService.getAllProducts();
        }
        return products.stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return toResponse(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request) {
        Category category = categoryRepository.findByName(request.category())
                .orElseGet(() -> categoryRepository.save(new Category(null, request.category(), null)));

        Product product = new Product(
                null,
                request.name(),
                request.brand(),
                request.description(),
                request.price(),
                request.inventory(),
                category,
                new ArrayList<>()
        );

        Product saved = productService.addProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getDescription(),
                product.getPrice(),
                product.getInventory(),
                product.getCategory() != null ? product.getCategory().getName() : null
        );
    }
}
