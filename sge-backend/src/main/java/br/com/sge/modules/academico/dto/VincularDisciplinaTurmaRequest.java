package br.com.sge.modules.academico.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record VincularDisciplinaTurmaRequest(
        @NotNull UUID disciplinaId,
        @NotNull UUID professorId) {}
