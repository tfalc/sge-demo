package br.com.sge.modules.academico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CriarTurmaRequest(
        @NotBlank @Size(max = 20) String nome,
        @NotNull UUID serieId,
        Integer capacidadeMax) {}
