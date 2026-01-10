package org.example.megasegashop.product.config;

import org.example.megasegashop.product.model.Category;
import org.example.megasegashop.product.model.Product;
import org.example.megasegashop.product.repository.CategoryRepository;
import org.example.megasegashop.product.repository.ProductRepository;
import org.example.megasegashop.product.service.product.IProductService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProductDataSeeder implements CommandLineRunner {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final IProductService productService;

    public ProductDataSeeder(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            IProductService productService
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productService = productService;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }

        Category consoles = categoryRepository.save(new Category(null, "Consoles", null));
        Category games = categoryRepository.save(new Category(null, "Games", null));
        Category accessories = categoryRepository.save(new Category(null, "Accessories", null));

        List<Product> products = List.of(
                new Product(null, "MegaSega Classic", "MegaSega", "Retro console bundle", new BigDecimal("129.99"), 15, consoles, new ArrayList<>()),
                new Product(null, "Sega Legend", "MegaSega", "Collector edition console", new BigDecimal("199.99"), 8, consoles, new ArrayList<>()),
                new Product(null, "Galaxy Racer", "SegaWorks", "Arcade racing game", new BigDecimal("39.99"), 120, games, new ArrayList<>()),
                new Product(null, "Fantasy Quest", "SegaWorks", "Adventure RPG", new BigDecimal("49.99"), 90, games, new ArrayList<>()),
                new Product(null, "Pro Controller", "MegaSega", "Wireless controller", new BigDecimal("59.99"), 40, accessories, new ArrayList<>())
        );

        for (Product product : products) {
            productService.addProduct(product);
        }
    }
}
