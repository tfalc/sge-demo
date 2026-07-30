package br.com.sge.modules.financeiro.openpix;

import br.com.sge.config.OpenPixProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class OpenPixClient {

    private static final Logger log = LoggerFactory.getLogger(OpenPixClient.class);

    private final OpenPixProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public OpenPixClient(OpenPixProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient =
                RestClient.builder().baseUrl(properties.resolvedBaseUrl()).build();
    }

    public Optional<OpenPixChargeResult> criarCobranca(
            UUID cobrancaId, int valorCentavos, String comentario, long expiraEmSegundos) {
        if (!properties.isConfigured()) {
            return Optional.empty();
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("correlationID", cobrancaId.toString());
        body.put("value", valorCentavos);
        body.put("comment", comentario);
        if (expiraEmSegundos > 0) {
            body.put("expiresIn", expiraEmSegundos);
        }

        try {
            String responseBody =
                    restClient
                            .post()
                            .uri("/api/v1/charge")
                            .header("Authorization", properties.appId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(body)
                            .retrieve()
                            .body(String.class);

            if (responseBody == null || responseBody.isBlank()) {
                log.warn("[OpenPix] Resposta vazia ao criar cobranca {}", cobrancaId);
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode charge = root.path("charge");
            String brCode = textOrNull(root, "brCode");
            if (brCode == null) {
                brCode = textOrNull(charge, "brCode");
            }
            String transactionId = textOrNull(charge, "transactionID");
            if (transactionId == null) {
                transactionId = textOrNull(charge, "identifier");
            }
            String qrImage = textOrNull(charge, "qrCodeImage");
            String status = textOrNull(charge, "status");

            if (brCode == null || brCode.isBlank()) {
                log.warn("[OpenPix] brCode ausente na resposta cobranca={}", cobrancaId);
                return Optional.empty();
            }

            log.info(
                    "[OpenPix] Cobranca criada correlationID={} transactionID={}",
                    cobrancaId,
                    transactionId);
            return Optional.of(new OpenPixChargeResult(transactionId, brCode, qrImage, status));
        } catch (RestClientResponseException e) {
            log.error(
                    "[OpenPix] Erro HTTP {} ao criar cobranca {}: {}",
                    e.getStatusCode().value(),
                    cobrancaId,
                    e.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            log.error("[OpenPix] Falha ao criar cobranca {}: {}", cobrancaId, e.getMessage());
            return Optional.empty();
        }
    }

    private static String textOrNull(JsonNode node, String field) {
        if (node == null || node.isMissingNode()) {
            return null;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text.isBlank() ? null : text;
    }
}
