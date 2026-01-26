package org.example.megasegashop.auth.service;

import org.example.megasegashop.auth.client.UserProfileClient;
import org.example.megasegashop.auth.dto.AuthResponse;
import org.example.megasegashop.auth.dto.LoginRequest;
import org.example.megasegashop.auth.dto.RegisterRequest;
import org.example.megasegashop.auth.dto.UserProfileResponse;
import org.example.megasegashop.auth.entity.AuthUser;
import org.example.megasegashop.auth.repository.AuthUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthUserRepository authUserRepository;

    @MockBean
    private UserProfileClient userProfileClient;

    @BeforeEach
    void setUp() {
        authUserRepository.deleteAll();
    }

    @Test
    void register_withExistingEmail_throwsConflict() {
        // Given
        String email = "existing@test.com";
        AuthUser existingUser = new AuthUser(null, email, "hash", "ROLE_USER", null);
        authUserRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest(email, "Password123", "Test", "User", null);

        // When/Then
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(request)
        );
        assertTrue(exception.getMessage().contains("Email already registered"));
    }

    @Test
    void login_withWrongPassword_throwsUnauthorized() {
        // Given
        String email = "user@test.com";
        AuthUser user = new AuthUser(null, email, "$2a$10$invalidhash", "ROLE_USER", null);
        authUserRepository.save(user);

        LoginRequest request = new LoginRequest(email, "wrongpassword");

        // When/Then
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(request)
        );
        assertTrue(exception.getMessage().contains("Invalid credentials"));
    }

    @Test
    void register_success_returnsToken() {
        // Given
        when(userProfileClient.createProfile(any()))
                .thenReturn(new UserProfileResponse(1L, 1L, "new@test.com", "New", "User", null));

        RegisterRequest request = new RegisterRequest(
                "new@test.com", "Password123", "New", "User", "+1234567890"
        );

        // When
        AuthResponse response = authService.register(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertNotNull(response.userId());

        verify(userProfileClient).createProfile(any());
    }
}
