package org.example.megasegashop.cart.service;

import feign.FeignException;
import org.example.megasegashop.cart.client.ProductClient;
import org.example.megasegashop.cart.dto.AddCartItemRequest;
import org.example.megasegashop.cart.dto.CartItemResponse;
import org.example.megasegashop.cart.dto.CartResponse;
import org.example.megasegashop.cart.dto.ProductResponse;
import org.example.megasegashop.cart.dto.RemoveCartItemRequest;
import org.example.megasegashop.cart.entity.Cart;
import org.example.megasegashop.cart.entity.CartItem;
import org.example.megasegashop.cart.repository.CartRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CartService {
    private final CartRepository cartRepository;
    private final ProductClient productClient;

    public CartService(CartRepository cartRepository, ProductClient productClient) {
        this.cartRepository = cartRepository;
        this.productClient = productClient;
    }

    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> new Cart(null, userId, new ArrayList<>(), null, null));
        return toResponse(cart);
    }

    @Transactional
    public CartResponse addItem(AddCartItemRequest request) {
        // Validate product exists and get actual price from product-service
        ProductResponse product = fetchProduct(request.productId());
        
        Cart cart = cartRepository.findByUserId(request.userId())
                .orElseGet(() -> new Cart(null, request.userId(), new ArrayList<>(), null, null));

        CartItem item = cart.getItems().stream()
                .filter(existing -> existing.getProductId().equals(request.productId()))
                .findFirst()
                .orElseGet(() -> {
                    CartItem created = new CartItem();
                    created.setProductId(request.productId());
                    created.setCart(cart);
                    cart.getItems().add(created);
                    return created;
                });

        // Use product name and price from product-service (not from client request!)
        item.setProductName(product.name());
        item.setUnitPrice(product.price());
        item.setQuantity(item.getQuantity() + request.quantity());

        Cart saved = cartRepository.save(cart);
        return toResponse(saved);
    }

    /**
     * Fetches product from product-service and validates it exists.
     * @throws ResponseStatusException if product not found or service unavailable
     */
    private ProductResponse fetchProduct(Long productId) {
        try {
            ProductResponse product = productClient.getProduct(productId);
            if (product == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Product not found: " + productId);
            }
            log.debug("Fetched product: id={}, name={}, price={}", 
                    product.id(), product.name(), product.price());
            return product;
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Product not found: " + productId);
        } catch (FeignException e) {
            log.error("Failed to fetch product {}: {}", productId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, 
                    "Product service is unavailable");
        }
    }

    @Transactional
    public CartResponse removeItem(RemoveCartItemRequest request) {
        Cart cart = cartRepository.findByUserId(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(request.productId()));
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found");
        }

        Cart saved = cartRepository.save(cart);
        return toResponse(saved);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> new CartItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();
        BigDecimal total = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(cart.getUserId(), items, total);
    }
}
