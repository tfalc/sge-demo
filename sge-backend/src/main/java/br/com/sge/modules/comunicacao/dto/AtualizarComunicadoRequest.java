package br.com.sge.modules.comunicacao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record AtualizarComunicadoRequest(
        @NotBlank @Size(max = 200) String titulo,
        @NotBlank String conteudo,
        @NotBlank String visivelPara,
        UUID turmaId) {}
