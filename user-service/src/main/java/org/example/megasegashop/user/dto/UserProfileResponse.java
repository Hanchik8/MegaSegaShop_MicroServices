package org.example.megasegashop.user.dto;

import java.time.Instant;

public record UserProfileResponse(
        Long id,
        Long authUserId,
        String email,
        String firstName,
        String lastName,
        Instant createdAt
) {
}
