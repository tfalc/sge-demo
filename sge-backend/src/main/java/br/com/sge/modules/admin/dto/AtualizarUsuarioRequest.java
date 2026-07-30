package br.com.sge.modules.admin.dto;

import br.com.sge.modules.cadastro.entity.PerfilUsuario;
import jakarta.validation.constraints.NotNull;

public record AtualizarUsuarioRequest(@NotNull PerfilUsuario perfil, @NotNull Boolean ativo) {}
