package org.example.megasegashop.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {
    private Email email = new Email();
    private Webhook webhook = new Webhook();
    private Sms sms = new Sms();

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public Webhook getWebhook() {
        return webhook;
    }

    public void setWebhook(Webhook webhook) {
        this.webhook = webhook;
    }

    public Sms getSms() {
        return sms;
    }

    public void setSms(Sms sms) {
        this.sms = sms;
    }

    public static class Email {
        private boolean enabled;
        private String from;
        private String subjectTemplate = "Your MegaSega order %s is placed";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getSubjectTemplate() {
            return subjectTemplate;
        }

        public void setSubjectTemplate(String subjectTemplate) {
            this.subjectTemplate = subjectTemplate;
        }
    }

    public static class Webhook {
        private boolean enabled;
        private String url;
        private String secret;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }

    public static class Sms {
        private boolean enabled;
        private String url;
        private String apiKey;
        private String apiKeyHeader = "Authorization";
        private String apiKeyPrefix = "Bearer";
        private String from = "MegaSega";
        private String defaultRecipient;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getApiKeyHeader() {
            return apiKeyHeader;
        }

        public void setApiKeyHeader(String apiKeyHeader) {
            this.apiKeyHeader = apiKeyHeader;
        }

        public String getApiKeyPrefix() {
            return apiKeyPrefix;
        }

        public void setApiKeyPrefix(String apiKeyPrefix) {
            this.apiKeyPrefix = apiKeyPrefix;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getDefaultRecipient() {
            return defaultRecipient;
        }

        public void setDefaultRecipient(String defaultRecipient) {
            this.defaultRecipient = defaultRecipient;
        }
    }
}
