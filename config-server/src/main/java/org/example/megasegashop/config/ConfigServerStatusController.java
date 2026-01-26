package org.example.megasegashop.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ConfigServerStatusController {
    @GetMapping("/")
    public Map<String, String> root() {
        return Map.of(
                "service", "config-server",
                "status", "ok",
                "example", "/application/default"
        );
    }
}
