package br.com.sge.modules.nutricao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CriarRestricaoAlimentarRequest(
        @NotNull UUID alunoId, @NotBlank String descricao, String severidade) {}
