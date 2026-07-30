package br.com.sge.modules.financeiro.openpix;

import br.com.sge.config.OpenPixProperties;
import org.springframework.stereotype.Component;

@Component
public class OpenPixWebhookValidator {

    private final OpenPixProperties properties;

    public OpenPixWebhookValidator(OpenPixProperties properties) {
        this.properties = properties;
    }

    /**
     * Quando OPENPIX_WEBHOOK_SECRET estiver definido, exige Authorization igual ao segredo
     * (padrao comum ao cadastrar webhook na OpenPix).
     */
    public void validarAuthorization(String authorizationHeader) {
        String secret = properties.webhookSecret();
        if (secret == null || secret.isBlank()) {
            return;
        }
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new IllegalArgumentException("Webhook PIX sem Authorization");
        }
        String token = authorizationHeader.trim();
        if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = token.substring(7).trim();
        }
        if (!secret.equals(token)) {
            throw new IllegalArgumentException("Webhook PIX com Authorization invalida");
        }
    }
}
