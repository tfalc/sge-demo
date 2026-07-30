package br.com.sge.modules.financeiro.service;

import br.com.sge.config.OpenPixProperties;
import br.com.sge.modules.financeiro.entity.Cobranca;
import br.com.sge.modules.financeiro.openpix.OpenPixChargeResult;
import br.com.sge.modules.financeiro.openpix.OpenPixClient;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Gera cobranca PIX: OpenPix quando {@code OPENPIX_APP_ID} estiver configurado; caso contrario simulacao local.
 */
@Service
public class FinancialPixService {

    private static final Logger log = LoggerFactory.getLogger(FinancialPixService.class);
    private static final ZoneId ZONA_BR = ZoneId.of("America/Sao_Paulo");

    public enum PixModo {
        SIMULACAO,
        OPENPIX
    }

    private final OpenPixProperties openPixProperties;
    private final OpenPixClient openPixClient;

    public FinancialPixService(OpenPixProperties openPixProperties, OpenPixClient openPixClient) {
        this.openPixProperties = openPixProperties;
        this.openPixClient = openPixClient;
    }

    public PixModo getModo() {
        return openPixProperties.isConfigured() ? PixModo.OPENPIX : PixModo.SIMULACAO;
    }

    public boolean isSimulacao() {
        return getModo() == PixModo.SIMULACAO;
    }

    /**
     * Preenche campos PIX da cobranca. Exige {@link Cobranca#getId()} ja persistido (correlationID OpenPix).
     */
    public void generatePixCharge(Cobranca cobranca) {
        if (cobranca.getId() == null) {
            throw new IllegalStateException("Cobranca deve ser salva antes de gerar PIX");
        }

        if (openPixProperties.isConfigured()) {
            Optional<OpenPixChargeResult> openPix = criarCobrancaOpenPix(cobranca);
            if (openPix.isPresent()) {
                aplicarResultadoOpenPix(cobranca, openPix.get());
                return;
            }
            log.warn(
                    "[PIX] OpenPix indisponivel para cobranca {}; usando simulacao local",
                    cobranca.getId());
        }

        gerarSimulacao(cobranca);
    }

    private Optional<OpenPixChargeResult> criarCobrancaOpenPix(Cobranca cobranca) {
        BigDecimal valor = cobranca.getValor() != null ? cobranca.getValor() : BigDecimal.ZERO;
        int centavos = valor.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValue();
        String comentario =
                String.format(Locale.ROOT, "Mensalidade SGE cobranca %s", cobranca.getId());
        long expiraEm = expiraEmSegundos(cobranca);
        return openPixClient.criarCobranca(cobranca.getId(), centavos, comentario, expiraEm);
    }

    private static long expiraEmSegundos(Cobranca cobranca) {
        if (cobranca.getVencimento() == null) {
            return 30L * 24 * 3600;
        }
        Instant limite =
                cobranca.getVencimento().plusDays(30).atStartOfDay(ZONA_BR).toInstant();
        long segundos = ChronoUnit.SECONDS.between(Instant.now(), limite);
        return Math.max(segundos, 86400L);
    }

    private void aplicarResultadoOpenPix(Cobranca cobranca, OpenPixChargeResult result) {
        String txid = result.transactionId();
        if (txid == null || txid.isBlank()) {
            txid = cobranca.getId().toString();
        }
        cobranca.setPixTxid(txid);
        cobranca.setPixQrcode(result.brCode());
        cobranca.setPixQrImageUrl(result.qrCodeImageUrl());
        log.info(
                "[PIX] Cobranca OpenPix: id={} txid={} status={}",
                cobranca.getId(),
                txid,
                result.status());
    }

    private void gerarSimulacao(Cobranca cobranca) {
        String pixTxId = UUID.randomUUID().toString();
        cobranca.setPixTxid(pixTxId);
        cobranca.setPixQrImageUrl(null);

        String emvPayload = buildFakeEmvCopyPaste(cobranca, pixTxId);
        String pixQrCode = Base64.getEncoder().encodeToString(emvPayload.getBytes(StandardCharsets.UTF_8));
        cobranca.setPixQrcode(pixQrCode);

        log.info(
                "[PIX] Cobranca simulada: id={} pixTxId={}",
                cobranca.getId(),
                pixTxId);
    }

    private static String buildFakeEmvCopyPaste(Cobranca cobranca, String pixTxId) {
        BigDecimal valor = cobranca.getValor() != null ? cobranca.getValor() : BigDecimal.ZERO;
        String valorStr = String.format(Locale.US, "%.2f", valor.doubleValue());
        return String.format(Locale.US, "SIMULACAO_OPENPIX|txid=%s|valor=%s|merchant=SGE", pixTxId, valorStr);
    }

    /** Decodifica copia-e-cola: EMV OpenPix, Base64 da simulacao ou texto legado. */
    public static String decodePixCopyPaste(String pixQrcode) {
        if (pixQrcode == null || pixQrcode.isBlank()) {
            return "";
        }
        String trimmed = pixQrcode.trim();
        if (trimmed.startsWith("000201")) {
            return trimmed;
        }
        try {
            return new String(Base64.getDecoder().decode(trimmed), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return trimmed;
        }
    }
}
