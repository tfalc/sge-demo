package br.com.sge.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais MVP: admin@sge.com / admin123 ou pai@sge.com / admin123")
public record LoginRequest(
        @Email(message = "Email invalido")
        @NotBlank(message = "Email e obrigatorio")
        @Schema(example = "admin@sge.com")
        String email,

        @NotBlank(message = "Senha e obrigatoria")
        @Schema(example = "admin123")
        String password
) {
}
