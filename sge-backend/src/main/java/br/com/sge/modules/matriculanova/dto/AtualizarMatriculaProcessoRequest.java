package br.com.sge.modules.matriculanova.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record AtualizarMatriculaProcessoRequest(
        UUID turmaPretendidaId,
        UUID responsavelId,
        @NotBlank @Size(max = 200) String candidatoNome,
        @Size(max = 20) String matriculaSugerida,
        @Size(max = 200) String responsavelNome,
        @Size(max = 200) String responsavelEmail,
        @Size(max = 30) String responsavelTelefone,
        @Size(max = 2000) String observacoes) {}
