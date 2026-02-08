package org.example.megasegashop.user.controller;

import jakarta.validation.Valid;
import org.example.megasegashop.user.dto.CreateUserProfileRequest;
import org.example.megasegashop.user.dto.UpdateUserProfileRequest;
import org.example.megasegashop.user.dto.UserProfileResponse;
import org.example.megasegashop.user.entity.UserProfile;
import org.example.megasegashop.user.service.UserProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/users")
public class UserProfileController {
    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @PostMapping
    public ResponseEntity<UserProfileResponse> create(@Valid @RequestBody CreateUserProfileRequest request) {
        // Profile creation is typically called by auth-service during registration
        // This is a service-to-service call, so no user authorization needed
        UserProfile profile = userProfileService.createProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(profile));
    }

    @GetMapping("/{id}")
    public UserProfileResponse getById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        UserProfile profile = userProfileService.getProfile(id);
        // Allow admins or the profile owner to view
        if (userEmail != null && !isAdmin(userRole) && !userEmail.equalsIgnoreCase(profile.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return toResponse(profile);
    }

    @GetMapping("/by-auth/{authUserId}")
    public UserProfileResponse getByAuthUserId(@PathVariable Long authUserId) {
        // This endpoint is typically used by other services internally
        // Service-to-service calls don't have user headers
        return toResponse(userProfileService.getProfileByAuthUserId(authUserId));
    }

    @PatchMapping("/{id}")
    public UserProfileResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserProfileRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        UserProfile profile = userProfileService.getProfile(id);
        // Allow admins or the profile owner to update
        if (userEmail != null && !isAdmin(userRole) && !userEmail.equalsIgnoreCase(profile.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return toResponse(userProfileService.updateProfile(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        UserProfile profile = userProfileService.getProfile(id);
        // Allow admins or the profile owner to delete
        if (userEmail != null && !isAdmin(userRole) && !userEmail.equalsIgnoreCase(profile.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        userProfileService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(String role) {
        return role != null && (role.equals("ROLE_ADMIN") || role.equals("ADMIN"));
    }

    private UserProfileResponse toResponse(UserProfile profile) {
        return new UserProfileResponse(
                profile.getId(),
                profile.getAuthUserId(),
                profile.getEmail(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getPhone(),
                profile.getCreatedAt()
        );
    }
}

