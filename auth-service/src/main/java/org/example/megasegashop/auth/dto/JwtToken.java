package org.example.megasegashop.auth.dto;

import java.time.Instant;

public record JwtToken(String token, Instant expiresAt) {
}
