package br.com.sge.modules.financeiro.openpix;

import br.com.sge.modules.financeiro.dto.PixWebhookPayload;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class OpenPixWebhookParser {

    public PixWebhookPayload parse(JsonNode body) {
        if (body == null || body.isNull()) {
            throw new IllegalArgumentException("Corpo do webhook vazio");
        }

        if (body.has("correlationID") && body.has("status")) {
            return new PixWebhookPayload(
                    body.path("correlationID").asText(null),
                    body.path("status").asText(null));
        }

        String event = body.path("event").asText("");
        JsonNode charge = body.path("charge");

        String correlationId = charge.path("correlationID").asText(null);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = body.path("correlationID").asText(null);
        }

        String status = charge.path("status").asText(null);
        if (status == null || status.isBlank()) {
            if (event.toUpperCase().contains("COMPLETED")) {
                status = "COMPLETED";
            } else if (event.toUpperCase().contains("EXPIRED")) {
                status = "EXPIRED";
            }
        }

        return new PixWebhookPayload(correlationId, status);
    }
}
