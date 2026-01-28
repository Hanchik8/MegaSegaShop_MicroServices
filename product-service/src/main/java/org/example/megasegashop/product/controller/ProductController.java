package org.example.megasegashop.product.controller;

import jakarta.validation.Valid;
import feign.FeignException;
import org.example.megasegashop.product.client.InventoryClient;
import org.example.megasegashop.product.dto.InventoryItemResponse;
import org.example.megasegashop.product.dto.ProductCreateRequest;
import org.example.megasegashop.product.dto.ProductResponse;
import org.example.megasegashop.product.dto.ProductStockResponse;
import org.example.megasegashop.product.dto.ProductUpdateRequest;
import org.example.megasegashop.product.model.Category;
import org.example.megasegashop.product.model.Product;
import org.example.megasegashop.product.repository.CategoryRepository;
import org.example.megasegashop.product.service.product.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.example.megasegashop.shared.web.AdminOnly;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final InventoryClient inventoryClient;

    public ProductController(
            ProductService productService,
            CategoryRepository categoryRepository,
            InventoryClient inventoryClient
    ) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.inventoryClient = inventoryClient;
    }

    @GetMapping
    public List<ProductResponse> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal price,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        List<Product> products = resolveProducts(category, brand, name, price, minPrice, maxPrice);
        return products.stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return toResponse(productService.getProductById(id));
    }

    @AdminOnly
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
                category,
                new ArrayList<>()
        );

        Product saved = productService.addProduct(product, request.initialQuantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @AdminOnly
    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest request) {
        Category category = categoryRepository.findByName(request.category())
                .orElseGet(() -> categoryRepository.save(new Category(null, request.category(), null)));
        return toResponse(productService.updateProduct(id, request, category));
    }

    @AdminOnly
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.deleteProductById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/stock")
    public ProductStockResponse getStock(@PathVariable Long id) {
        productService.getProductById(id);
        try {
            InventoryItemResponse item = inventoryClient.getInventory(id);
            if (item == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found");
            }
            return new ProductStockResponse(item.productId(), item.availableQuantity());
        } catch (FeignException.NotFound ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found");
        } catch (FeignException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Inventory service unavailable");
        }
    }

    private List<Product> resolveProducts(
            String category,
            String brand,
            String name,
            BigDecimal price,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        if (minPrice != null || maxPrice != null) {
            if (minPrice == null || maxPrice == null) {
                throw unsupportedSearch("minPrice and maxPrice must be provided together");
            }
            if (category != null || brand != null || name != null || price != null) {
                throw unsupportedSearch("price range cannot be combined with other filters");
            }
            return productService.getProductsByPriceRange(minPrice, maxPrice);
        }

        if (price != null) {
            if (category != null && name != null && brand == null) {
                return productService.getProductsByCategoryAndPriceAndName(category, price, name);
            }
            if (category != null && name == null && brand == null) {
                return productService.getProductsByCategoryAndPrice(category, price);
            }
            if (category == null && brand == null && name == null) {
                return productService.getProductsByPrice(price);
            }
            throw unsupportedSearch("unsupported filter combination with price");
        }

        if (name != null) {
            if (brand != null && category == null) {
                return productService.getProductsByBrandAndName(brand, name);
            }
            if (brand == null && category == null) {
                return productService.getProductsByName(name);
            }
            throw unsupportedSearch("unsupported filter combination with name");
        }

        if (category != null && brand != null) {
            return productService.getProductsByCategoryAndBrand(category, brand);
        }
        if (category != null) {
            return productService.getProductsByCategory(category);
        }
        if (brand != null) {
            return productService.getProductsByBrand(brand);
        }
        return productService.getAllProducts();
    }

    private ResponseStatusException unsupportedSearch(String reason) {
        String message = reason + ". Supported filters: " +
                "category, brand, category+brand, name, brand+name, price, category+price, " +
                "category+price+name, minPrice+maxPrice.";
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory() != null ? product.getCategory().getName() : null
        );
    }
}
