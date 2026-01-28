package org.example.megasegashop.product.repository;

import org.example.megasegashop.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByPriceBetween(BigDecimal min, BigDecimal max);
    List<Product> findByCategoryName(String category);
    List<Product> findByBrand(String brand);
    List<Product> findByCategoryNameAndBrand(String category, String brand);
    List<Product> findByCategoryNameAndPrice(String category, BigDecimal price);
    List<Product> findByName(String name);
    List<Product> findByPrice(BigDecimal price);
    List<Product> findByCategoryNameAndPriceAndName(String category, BigDecimal price, String name);
    List<Product> findByBrandAndName(String brand, String name);
}
