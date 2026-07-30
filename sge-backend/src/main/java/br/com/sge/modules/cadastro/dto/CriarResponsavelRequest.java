package br.com.sge.modules.cadastro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CriarResponsavelRequest(
        @NotBlank @Size(max = 200) String nome,
        @NotBlank @Email @Size(max = 200) String email,
        @Size(max = 50) String grauParentesco,
        UUID alunoId,
        @Size(min = 6, max = 100) String senha) {}
