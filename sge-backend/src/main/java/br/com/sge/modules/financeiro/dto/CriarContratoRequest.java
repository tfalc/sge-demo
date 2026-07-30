package br.com.sge.modules.financeiro.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CriarContratoRequest(
        @NotNull UUID alunoId,
        @NotNull UUID planoId,
        LocalDate dataInicio) {}
