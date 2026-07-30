package br.com.sge.modules.academico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarDisciplinaRequest(
        @NotBlank @Size(max = 100) String nome,
        @Size(max = 20) String codigo) {}
