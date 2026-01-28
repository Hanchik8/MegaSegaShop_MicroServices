package org.example.megasegashop.order.dto;

public record UserProfileSnapshot(
        Long id,
        Long authUserId,
        String email,
        String phone
) {
}
