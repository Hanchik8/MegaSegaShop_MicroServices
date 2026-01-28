package org.example.megasegashop.auth.service;

import io.jsonwebtoken.security.Keys;
import org.example.megasegashop.auth.dto.JwtToken;
import org.example.megasegashop.shared.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-minutes}") long expirationMinutes
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    public JwtToken issueToken(String subject, String role) {
        Object[] result = JwtTokenUtil.generateTokenWithExpiry(subject, role, key, expirationMinutes);
        return new JwtToken((String) result[0], (Instant) result[1]);
    }

    public boolean isValid(String token) {
        return JwtTokenUtil.isValid(token, key);
    }

    public String extractSubject(String token) {
        return JwtTokenUtil.extractSubject(token, key);
    }

    public String extractRole(String token) {
        return JwtTokenUtil.extractRole(token, key);
    }
}
