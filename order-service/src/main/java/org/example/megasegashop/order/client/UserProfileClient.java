package org.example.megasegashop.order.client;

import org.example.megasegashop.order.dto.UserProfileSnapshot;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserProfileClient {
    @GetMapping("/users/by-auth/{authUserId}")
    UserProfileSnapshot getByAuthUserId(@PathVariable("authUserId") Long authUserId);
}
