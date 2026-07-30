package br.com.sge.modules.financeiro.openpix;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.com.sge.modules.financeiro.dto.PixWebhookPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class OpenPixWebhookParserTest {

    private final OpenPixWebhookParser parser = new OpenPixWebhookParser();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parsePayloadSimplificado() throws Exception {
        PixWebhookPayload payload =
                parser.parse(objectMapper.readTree("""
                        {"correlationID":"a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","status":"COMPLETED"}
                        """));
        assertEquals("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11", payload.correlationID());
        assertEquals("COMPLETED", payload.status());
    }

    @Test
    void parseWebhookOpenPix() throws Exception {
        PixWebhookPayload payload =
                parser.parse(objectMapper.readTree("""
                        {
                          "event": "OPENPIX:CHARGE_COMPLETED",
                          "charge": {
                            "correlationID": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22",
                            "status": "COMPLETED"
                          }
                        }
                        """));
        assertEquals("b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22", payload.correlationID());
        assertEquals("COMPLETED", payload.status());
    }

    @Test
    void parseEventoSemStatusNaCharge() throws Exception {
        PixWebhookPayload payload =
                parser.parse(objectMapper.readTree("""
                        {
                          "event": "OPENPIX:CHARGE_COMPLETED",
                          "charge": {
                            "correlationID": "c1eebc99-9c0b-4ef8-bb6d-6bb9bd380a33"
                          }
                        }
                        """));
        assertEquals("c1eebc99-9c0b-4ef8-bb6d-6bb9bd380a33", payload.correlationID());
        assertEquals("COMPLETED", payload.status());
    }
}
