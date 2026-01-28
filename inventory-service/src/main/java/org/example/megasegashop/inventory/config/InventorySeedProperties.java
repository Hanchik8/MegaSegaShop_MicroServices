package org.example.megasegashop.inventory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "inventory.seed")
public class InventorySeedProperties {
    private boolean enabled;
    private String productServiceUrl = "http://product-service";
    private int defaultQuantity;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getProductServiceUrl() {
        return productServiceUrl;
    }

    public void setProductServiceUrl(String productServiceUrl) {
        this.productServiceUrl = productServiceUrl;
    }

    public int getDefaultQuantity() {
        return defaultQuantity;
    }

    public void setDefaultQuantity(int defaultQuantity) {
        this.defaultQuantity = defaultQuantity;
    }
}
