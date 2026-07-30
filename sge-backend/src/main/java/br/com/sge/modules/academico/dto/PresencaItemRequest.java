package br.com.sge.modules.academico.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PresencaItemRequest(
        @NotNull UUID alunoId, @NotNull Boolean presente, String justificativa) {}
