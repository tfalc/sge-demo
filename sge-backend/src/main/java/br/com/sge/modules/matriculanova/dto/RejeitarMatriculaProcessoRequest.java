package br.com.sge.modules.matriculanova.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejeitarMatriculaProcessoRequest(@NotBlank @Size(max = 1000) String motivo) {}
