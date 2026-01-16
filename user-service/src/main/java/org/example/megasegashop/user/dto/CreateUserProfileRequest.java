package org.example.megasegashop.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserProfileRequest(
        @NotNull Long authUserId,
        @Email @NotBlank String email,
        @NotBlank String firstName,
        @NotBlank String lastName,
        String phone
) {
}
