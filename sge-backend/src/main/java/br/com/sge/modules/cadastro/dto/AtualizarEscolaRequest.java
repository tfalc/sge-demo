package br.com.sge.modules.cadastro.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AtualizarEscolaRequest(
        @NotBlank @Size(max = 200) String nome,
        @Size(max = 18) String cnpj,
        @NotNull @DecimalMin("0.0") @DecimalMax("10.0") Double notaMinimaAprovacao,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") Double frequenciaMinima) {}
