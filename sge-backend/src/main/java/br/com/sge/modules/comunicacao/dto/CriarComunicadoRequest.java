package br.com.sge.modules.comunicacao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CriarComunicadoRequest(
        @NotBlank @Size(max = 200) String titulo,
        @NotBlank String conteudo,
        @NotNull String visivelPara,
        UUID turmaId) {}
