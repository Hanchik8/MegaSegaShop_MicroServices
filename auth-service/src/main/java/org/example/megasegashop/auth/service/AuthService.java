package org.example.megasegashop.auth.service;

import org.example.megasegashop.auth.client.UserProfileClient;
import org.example.megasegashop.auth.dto.AuthResponse;
import org.example.megasegashop.auth.dto.CreateUserProfileRequest;
import org.example.megasegashop.auth.dto.JwtToken;
import org.example.megasegashop.auth.dto.LoginRequest;
import org.example.megasegashop.auth.dto.RegisterRequest;
import org.example.megasegashop.auth.dto.UserProfileResponse;
import org.example.megasegashop.auth.entity.AuthUser;
import org.example.megasegashop.auth.repository.AuthUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserProfileClient userProfileClient;

    public AuthService(
            AuthUserRepository authUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UserProfileClient userProfileClient
    ) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userProfileClient = userProfileClient;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (authUserRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        AuthUser user = new AuthUser(
                null,
                request.email(),
                passwordEncoder.encode(request.password()),
                "ROLE_USER",
                null
        );

        AuthUser savedUser = authUserRepository.save(user);
        UserProfileResponse profile = userProfileClient.createProfile(
                new CreateUserProfileRequest(
                        savedUser.getId(),
                        savedUser.getEmail(),
                        request.firstName(),
                        request.lastName()
                )
        );
        savedUser.setProfileId(profile.id());
        authUserRepository.save(savedUser);

        JwtToken token = jwtService.issueToken(savedUser.getEmail(), savedUser.getRole());
        return new AuthResponse(token.token(), "Bearer", token.expiresAt(), savedUser.getId(), savedUser.getProfileId());
    }

    public AuthResponse login(LoginRequest request) {
        AuthUser user = authUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        JwtToken token = jwtService.issueToken(user.getEmail(), user.getRole());
        return new AuthResponse(token.token(), "Bearer", token.expiresAt(), user.getId(), user.getProfileId());
    }
}
