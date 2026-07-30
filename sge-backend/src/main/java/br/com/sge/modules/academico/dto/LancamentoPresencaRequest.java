package br.com.sge.modules.academico.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record LancamentoPresencaRequest(
        @NotNull UUID turmaDisciplinaProfessorId,
        @NotNull LocalDate dataAula,
        @NotEmpty List<@Valid PresencaItemRequest> presencas) {}
