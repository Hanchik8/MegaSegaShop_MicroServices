package org.example.megasegashop.order.client;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

public class InventoryClientConfig {
    private static final String USER_ROLE_HEADER = "X-User-Role";
    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    @Bean
    public RequestInterceptor inventoryAdminRoleInterceptor() {
        return requestTemplate -> requestTemplate.header(USER_ROLE_HEADER, ADMIN_ROLE);
    }
}
