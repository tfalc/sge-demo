package br.com.sge.modules.convivencia.dto;

import br.com.sge.modules.convivencia.entity.TipoOcorrencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record RegistrarOcorrenciaRequest(
        @NotNull UUID alunoId,
        @NotNull UUID turmaDisciplinaProfessorId,
        @NotNull LocalDate dataOcorrencia,
        @NotNull TipoOcorrencia tipo,
        @NotBlank @Size(max = 2000) String descricao) {}
