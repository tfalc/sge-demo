package br.com.sge.modules.financeiro.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Payload simplificado do webhook PIX (ex.: OpenPix). correlationID = id da cobranca no nosso sistema.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PixWebhookPayload(String correlationID, String status) {}
