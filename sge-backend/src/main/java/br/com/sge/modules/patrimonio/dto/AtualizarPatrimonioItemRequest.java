package br.com.sge.modules.patrimonio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AtualizarPatrimonioItemRequest(
        String nome,
        String categoria,
        String localizacao,
        String numeroPatrimonio,
        LocalDate dataAquisicao,
        BigDecimal valorAquisicao,
        String status,
        String observacoes) {}
