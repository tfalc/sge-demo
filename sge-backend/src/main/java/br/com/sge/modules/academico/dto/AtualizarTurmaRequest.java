package br.com.sge.modules.academico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record AtualizarTurmaRequest(
        @NotBlank @Size(max = 100) String nome,
        @NotNull UUID serieId) {}
