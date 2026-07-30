package br.com.sge.modules.patrimonio.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CriarPatrimonioItemRequest(
        @NotBlank String nome,
        String categoria,
        String localizacao,
        String numeroPatrimonio,
        LocalDate dataAquisicao,
        BigDecimal valorAquisicao,
        String status,
        String observacoes) {}
