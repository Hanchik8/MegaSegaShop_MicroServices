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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserProfileClient userProfileClient;
    private final TransactionTemplate requiresNewTransaction;

    public AuthService(
            AuthUserRepository authUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UserProfileClient userProfileClient,
            PlatformTransactionManager transactionManager
    ) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userProfileClient = userProfileClient;
        this.requiresNewTransaction = new TransactionTemplate(transactionManager);
        this.requiresNewTransaction.setPropagationBehaviorName("PROPAGATION_REQUIRES_NEW");
    }

    public AuthResponse register(RegisterRequest request) {
        // Use pessimistic lock inside transaction to prevent race condition
        // where two concurrent registrations pass the existence check
        AuthUser savedUser = requiresNewTransaction.execute(status -> {
            // Check with lock to prevent concurrent duplicate registrations
            if (authUserRepository.findWithLockByEmail(request.email()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
            }
            return authUserRepository.save(
                    new AuthUser(
                            null,
                            request.email(),
                            passwordEncoder.encode(request.password()),
                            "ROLE_USER",
                            null
                    )
            );
        });

        if (savedUser == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create user");
        }

        UserProfileResponse profile;
        try {
            profile = userProfileClient.createProfile(
                    new CreateUserProfileRequest(
                            savedUser.getId(),
                            savedUser.getEmail(),
                            request.firstName(),
                            request.lastName(),
                            request.phone()
                    )
            );
        } catch (Exception ex) {
            requiresNewTransaction.executeWithoutResult(status -> authUserRepository.deleteById(savedUser.getId()));
            throw ex;
        }

        try {
            AuthUser userWithProfile = savedUser;
            requiresNewTransaction.executeWithoutResult(status -> {
                AuthUser reloaded = authUserRepository.findById(userWithProfile.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
                reloaded.setProfileId(profile.id());
                authUserRepository.save(reloaded);
            });
            savedUser.setProfileId(profile.id());
        } catch (Exception ex) {
            try {
                userProfileClient.deleteProfile(profile.id());
            } catch (Exception compensationEx) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to link user profile; manual cleanup required"
                );
            }
            throw ex;
        }

        JwtToken token = jwtService.issueToken(savedUser.getEmail(), savedUser.getRole(), savedUser.getId());
        return new AuthResponse(token.token(), "Bearer", token.expiresAt(), savedUser.getId(), savedUser.getProfileId());
    }

    public AuthResponse login(LoginRequest request) {
        AuthUser user = authUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        JwtToken token = jwtService.issueToken(user.getEmail(), user.getRole(), user.getId());
        return new AuthResponse(token.token(), "Bearer", token.expiresAt(), user.getId(), user.getProfileId());
    }
}
