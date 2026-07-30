package br.com.sge.modules.cadastro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CriarAlunoRequest(
        @NotBlank @Size(max = 200) String nome,
        @NotBlank @Size(max = 20) String matricula,
        @NotNull UUID turmaId) {}
