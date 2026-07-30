package br.com.sge.modules.academico.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.UUID;

public record AtualizarHorarioRequest(
        @NotNull @Min(1) @Max(5) Short diaSemana,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFim,
        @NotNull UUID disciplinaId,
        UUID professorId) {}
