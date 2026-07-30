package br.com.sge.modules.colegiado.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.UUID;

public record CriarEncaminhamentoRequest(
        @NotBlank String descricao, UUID responsavelUsuarioId, String responsavelNome, LocalDate prazo) {}
