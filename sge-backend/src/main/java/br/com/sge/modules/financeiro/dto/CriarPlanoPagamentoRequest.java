package br.com.sge.modules.financeiro.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CriarPlanoPagamentoRequest(
        @NotBlank @Size(max = 100) String nome,
        @NotNull @DecimalMin("0.01") BigDecimal valorMensalidade,
        @Min(1) @Max(28) int diaVencimento) {}
