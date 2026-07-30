package br.com.sge.modules.financeiro.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CriarCobrancaRequest(
        @NotNull UUID contratoId,
        @NotNull LocalDate competencia,
        @NotNull BigDecimal valor,
        @NotNull LocalDate vencimento) {}
