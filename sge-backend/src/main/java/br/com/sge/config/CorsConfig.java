package br.com.sge.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public record CorsConfig(List<String> allowedOrigins, List<String> allowedOriginPatterns) {

    public CorsConfig {
        if (allowedOrigins == null) {
            allowedOrigins = List.of();
        }
        if (allowedOriginPatterns == null) {
            allowedOriginPatterns = List.of();
        }
    }

    /**
     * Permite override por env {@code APP_CORS_ALLOWED_ORIGIN_PATTERNS} (lista separada por vírgula).
     * Se a env estiver vazia, mantém os patterns do YAML.
     */
    public List<String> effectiveOriginPatterns() {
        String fromEnv = System.getenv("APP_CORS_ALLOWED_ORIGIN_PATTERNS");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return Arrays.stream(fromEnv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
        return allowedOriginPatterns;
    }
}
