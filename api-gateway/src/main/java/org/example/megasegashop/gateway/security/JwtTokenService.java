package org.example.megasegashop.gateway.security;

import io.jsonwebtoken.security.Keys;
import org.example.megasegashop.shared.security.JwtTokenUtil;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtTokenService {
    private final SecretKey key;

    public JwtTokenService(SecurityProperties securityProperties) {
        this.key = Keys.hmacShaKeyFor(securityProperties.getSecret().getBytes(StandardCharsets.UTF_8));
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
