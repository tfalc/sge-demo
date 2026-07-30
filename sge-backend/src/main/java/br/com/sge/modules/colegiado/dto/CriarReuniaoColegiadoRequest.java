package br.com.sge.modules.colegiado.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CriarReuniaoColegiadoRequest(
        @NotBlank String titulo,
        String tipo,
        UUID turmaId,
        @NotNull LocalDate dataReuniao,
        String horaReuniao,
        String pauta,
        List<UUID> participanteUsuarioIds) {}
