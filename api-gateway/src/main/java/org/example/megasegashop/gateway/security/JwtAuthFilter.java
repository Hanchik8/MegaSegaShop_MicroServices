package org.example.megasegashop.gateway.security;

import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {
    private final JwtTokenService jwtTokenService;
    private final SecurityProperties securityProperties;

    public JwtAuthFilter(JwtTokenService jwtTokenService, SecurityProperties securityProperties) {
        this.jwtTokenService = jwtTokenService;
        this.securityProperties = securityProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublicPath(path) || isPublicProductRead(exchange) || isPublicImageRead(exchange)
                || isPublicUiPath(exchange, path)) {
            return chain.filter(exchange);
        }

        String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = header.substring(7);
        if (!jwtTokenService.isValid(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String email = jwtTokenService.extractSubject(token);
        String role = jwtTokenService.extractRole(token);
        Long userId = jwtTokenService.extractUserId(token);

        var requestBuilder = exchange.getRequest().mutate()
                .header("X-User-Email", email)
                .header("X-User-Role", role);
        
        if (userId != null) {
            requestBuilder.header("X-User-Id", userId.toString());
        }

        return chain.filter(exchange.mutate()
                .request(requestBuilder.build())
                .build());
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isPublicPath(String path) {
        for (String prefix : securityProperties.getPublicPaths()) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPublicProductRead(ServerWebExchange exchange) {
        return exchange.getRequest().getMethod() == HttpMethod.GET
                && exchange.getRequest().getURI().getPath().startsWith("/api/products");
    }

    private boolean isPublicImageRead(ServerWebExchange exchange) {
        return exchange.getRequest().getMethod() == HttpMethod.GET
                && exchange.getRequest().getURI().getPath().startsWith("/api/images");
    }

    private boolean isPublicUiPath(ServerWebExchange exchange, String path) {
        if (exchange.getRequest().getMethod() != HttpMethod.GET) {
            return false;
        }
        return "/".equals(path)
                || "/index.html".equals(path)
                || path.startsWith("/assets/")
                || path.startsWith("/favicon");
    }
}
