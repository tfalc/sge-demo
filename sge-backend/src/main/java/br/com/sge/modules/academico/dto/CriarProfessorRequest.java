package br.com.sge.modules.academico.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarProfessorRequest(
        @NotBlank @Size(max = 200) String nome,
        @NotBlank @Email @Size(max = 200) String email,
        @Size(max = 50) String registroMec,
        @Size(min = 6, max = 100) String senha) {}
