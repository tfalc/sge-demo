package br.com.sge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.openpix")
public record OpenPixProperties(String appId, String webhookSecret, String baseUrl) {

    public boolean isConfigured() {
        return appId != null && !appId.isBlank();
    }

    public String resolvedBaseUrl() {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "https://api.openpix.com.br";
        }
        return baseUrl.replaceAll("/$", "");
    }
}
