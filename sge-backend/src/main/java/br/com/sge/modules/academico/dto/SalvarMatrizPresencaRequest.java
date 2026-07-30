package br.com.sge.modules.academico.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SalvarMatrizPresencaRequest(
        @NotNull UUID turmaDisciplinaProfessorId,
        @NotNull UUID periodoId,
        Integer aulasPrevistas,
        @NotEmpty List<MatrizPresencaCelulaRequest> celulas) {}
