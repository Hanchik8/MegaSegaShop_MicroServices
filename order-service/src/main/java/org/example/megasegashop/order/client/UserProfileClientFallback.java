package org.example.megasegashop.order.client;

import org.example.megasegashop.order.dto.UserProfileSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback implementation for UserProfileClient when user-service is unavailable.
 */
@Slf4j
@Component
public class UserProfileClientFallback implements UserProfileClient {

    @Override
    public UserProfileSnapshot getByAuthUserId(Long authUserId) {
        log.warn("Circuit breaker fallback: user-service unavailable for authUserId={}", authUserId);
        return null;
    }
}
