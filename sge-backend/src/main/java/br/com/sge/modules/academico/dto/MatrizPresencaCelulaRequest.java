package br.com.sge.modules.academico.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record MatrizPresencaCelulaRequest(
        @NotNull UUID alunoId,
        @NotNull LocalDate dataAula,
        @NotNull Boolean presente,
        String justificativa) {}
