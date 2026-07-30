package br.com.sge.modules.saude.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record CriarAgendamentoSaudeRequest(
        @NotNull UUID alunoId,
        @NotNull Instant dataHora,
        String observacoes,
        Boolean privado) {}
