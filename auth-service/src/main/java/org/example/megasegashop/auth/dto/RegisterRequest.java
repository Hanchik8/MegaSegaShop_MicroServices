package org.example.megasegashop.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$",
                message = "Password must be at least 8 characters with at least one letter and one digit"
        )
        String password,
        @NotBlank String firstName,
        @NotBlank String lastName,
        String phone
) {
}
