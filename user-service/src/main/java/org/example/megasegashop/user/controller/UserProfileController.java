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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserProfileController {
    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @PostMapping
    public ResponseEntity<UserProfileResponse> create(@Valid @RequestBody CreateUserProfileRequest request) {
        UserProfile profile = userProfileService.createProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(profile));
    }

    @GetMapping("/{id}")
    public UserProfileResponse getById(@PathVariable Long id) {
        return toResponse(userProfileService.getProfile(id));
    }

    @GetMapping("/by-auth/{authUserId}")
    public UserProfileResponse getByAuthUserId(@PathVariable Long authUserId) {
        return toResponse(userProfileService.getProfileByAuthUserId(authUserId));
    }

    @PatchMapping("/{id}")
    public UserProfileResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        UserProfile profile = userProfileService.updateProfile(id, request);
        return toResponse(profile);
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
