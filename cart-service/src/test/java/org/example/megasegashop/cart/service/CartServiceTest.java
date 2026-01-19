package org.example.megasegashop.cart.service;

import org.example.megasegashop.cart.client.ProductClient;
import org.example.megasegashop.cart.dto.AddCartItemRequest;
import org.example.megasegashop.cart.dto.CartResponse;
import org.example.megasegashop.cart.dto.ProductResponse;
import org.example.megasegashop.cart.entity.Cart;
import org.example.megasegashop.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @MockBean
    private ProductClient productClient;

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();
    }

    @Test
    void addItem_createsNewCartItem() {
        // Given
        Long userId = 1L;
        Long productId = 100L;
        when(productClient.getProduct(productId))
                .thenReturn(new ProductResponse(productId, "Test Product", "Brand", "Desc", new BigDecimal("29.99"), "Category"));

        AddCartItemRequest request = new AddCartItemRequest(userId, productId, 2);

        // When
        CartResponse response = cartService.addItem(request);

        // Then
        assertNotNull(response);
        assertEquals(userId, response.userId());
        assertEquals(1, response.items().size());
        assertEquals(productId, response.items().get(0).productId());
        assertEquals(2, response.items().get(0).quantity());
        assertEquals(new BigDecimal("29.99"), response.items().get(0).unitPrice());

        verify(productClient).getProduct(productId);
    }

    @Test
    void clearCart_removesAllItems() {
        // Given
        Long userId = 2L;
        Cart cart = new Cart(null, userId, new ArrayList<>(), null, null);
        cartRepository.save(cart);

        when(productClient.getProduct(anyLong()))
                .thenReturn(new ProductResponse(1L, "Product", "Brand", "Desc", new BigDecimal("10.00"), "Cat"));

        cartService.addItem(new AddCartItemRequest(userId, 1L, 1));
        CartResponse beforeClear = cartService.getCart(userId);
        assertFalse(beforeClear.items().isEmpty());

        // When
        cartService.clearCart(userId);

        // Then
        CartResponse afterClear = cartService.getCart(userId);
        assertTrue(afterClear.items().isEmpty());
    }
}
