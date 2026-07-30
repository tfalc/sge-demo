package br.com.sge.modules.auth.dto;

import java.util.List;
import java.util.UUID;

public record UserMeResponse(
        UUID usuarioId,
        String nome,
        String email,
        String telefone,
        String perfil,
        UUID responsavelId,
        UUID professorId,
        UUID profissionalSaudeId,
        UUID alunoId,
        UUID turmaId,
        String turmaNome,
        List<FilhoResumo> filhos,
        List<String> areasMenu) {}
