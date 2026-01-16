package org.example.megasegashop.user.dto;

public record UpdateUserProfileRequest(
        String firstName,
        String lastName,
        String phone
) {
}
