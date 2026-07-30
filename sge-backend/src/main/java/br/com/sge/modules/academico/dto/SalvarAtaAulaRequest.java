package br.com.sge.modules.academico.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record SalvarAtaAulaRequest(
        @NotNull UUID turmaDisciplinaProfessorId,
        @NotNull LocalDate dataAula,
        String conteudo,
        String tarefaCasa,
        String observacoes) {}
