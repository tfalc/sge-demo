package br.com.sge.modules.academico.dto;

import br.com.sge.modules.academico.entity.TipoNota;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record LancarNotaRequest(
        @NotNull UUID alunoId,
        @NotNull UUID turmaDisciplinaProfessorId,
        @NotNull UUID periodoId,
        @NotNull
                @DecimalMin("0.0")
                @DecimalMax("10.0")
                BigDecimal valor,
        @NotNull TipoNota tipo,
        String observacao) {}
