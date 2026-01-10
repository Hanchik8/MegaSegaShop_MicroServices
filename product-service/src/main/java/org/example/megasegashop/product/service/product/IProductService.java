package org.example.megasegashop.product.service.product;

import org.example.megasegashop.product.model.Product;

import java.math.BigDecimal;
import java.util.List;

public interface IProductService {
    Product addProduct(Product product);
    List<Product> getAllProducts();
    Product getProductById(Long id);
    void deleteProductById(Long id);
    void updateProduct(Product product);
    List<Product> getProductsByPriceRange(BigDecimal min, BigDecimal max);
    List<Product> getProductsByCategory(String category);

    List<Product> getProductsByName(String name);
    List<Product> getProductsByPrice(BigDecimal price);
    List<Product> getProductsByBrand(String brand);
    List<Product> getProductsByCategoryAndPrice(String category, BigDecimal price);
    List<Product> getProductsByCategoryAndBrand(String category, String brand);
    List<Product> getProductsByCategoryAndPriceAndName(String category, BigDecimal price, String name);
    List<Product> getProductsByBrandAndName(String brand, String name);
}
