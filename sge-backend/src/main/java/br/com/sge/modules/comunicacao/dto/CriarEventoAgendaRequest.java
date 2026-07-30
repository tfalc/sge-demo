package br.com.sge.modules.comunicacao.dto;

import br.com.sge.modules.comunicacao.entity.TipoEvento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record CriarEventoAgendaRequest(
        @NotBlank @Size(max = 200) String titulo,
        String descricao,
        @NotNull Instant dataInicio,
        Instant dataFim,
        TipoEvento tipo,
        UUID turmaId) {}
