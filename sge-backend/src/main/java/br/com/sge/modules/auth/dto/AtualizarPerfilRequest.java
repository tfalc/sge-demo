package br.com.sge.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtualizarPerfilRequest(
        @NotBlank @Size(max = 200) String nome,
        @Email @NotBlank @Size(max = 200) String email,
        @Size(max = 20) String telefone) {}
