package org.example.megasegashop.auth.dto;

public record UserProfileResponse(
        Long id,
        Long authUserId,
        String email,
        String firstName,
        String lastName
) {
}
